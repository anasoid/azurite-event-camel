package org.anasoid.azurite.event.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AzuriteEventFromLog extends RouteBuilder {

    private final static Boolean SKIP_LINE = true;
    private final static LoggingLevel DEBUG_LEVEL = LoggingLevel.DEBUG;
    private final static String CONF_AZURITE_URL = "AZURITE_URL";
    private final static String CONF_AZURITE_LOG_FOLDER = "AZURITE_LOG_FOLDER";
    private final static String CONF_CAMEL_SEEK_FOLDER = "CAMEL_SEEK_FOLDER";
    private final static String FILE_NAME = "access-azurite";
    private final static String FILE_PATH = System.getenv().getOrDefault(CONF_AZURITE_LOG_FOLDER, "./compose/.logs");
    private final static String FILE_PATH_SEEK = System.getenv().getOrDefault(CONF_CAMEL_SEEK_FOLDER, FILE_PATH);
    private final static String FILE_FULL_NAME = FILE_PATH + "/" + FILE_NAME + ".log";
    private final static String FILE_FULL_SEEK_NAME = FILE_PATH_SEEK + "/" + FILE_NAME + ".seek";
    protected final static String DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss XX";
    public final static String EVENT_DATA_KEY = "event.data";
    public final static String EVENT_LINE_KEY = "event.line";


    @Override
    public void configure() throws Exception {

        from("stream:file?fileName=" + FILE_FULL_NAME + "&scanStream=true&scanStreamDelay=1000")
                .process(new FileSkipConsumedLineProcessor())
                .choice()
                .when(simple("${variable.skip_line} == '" + SKIP_LINE.toString().toLowerCase() + "'"))
                .log(DEBUG_LEVEL, "skip line : [${variable.event.line} ]")
                .otherwise()
                .to("direct:processLine")
                .endChoice();
        from("direct:processLine")
                .process(new ParseLineProcessor())
                .choice()
                .when(simple("${variable.event.data} == null "))
                .log(DEBUG_LEVEL, "skip line not access : [ ${variable.event.line} ]")
                .otherwise()
                .to("direct:processAccessLine")
                .endChoice();
        from("direct:processAccessLine")
                .process(new AzuriteEventGeneratorProcessor())
                .choice()
                .when(simple("${variable.skip_line} == 'true' "))
                .log(DEBUG_LEVEL, "skip line not create or delete : [ ${variable.event.line} ]")
                .otherwise()
                .log("<<<<${header.CamelStreamIndex}|${variable.old_position}|${variable.skip_line}>>>> [ ${body} ]")
                .to("direct:sendToKafka")
                .endChoice();
    }

    /**
     *
     */
    class AzuriteEventGeneratorProcessor implements Processor {
        @Override
        public void process(Exchange exchange) {

            EventData eventData = (EventData) exchange.getVariable(EVENT_DATA_KEY);

            if (eventData != null) {
                if (("PUT".equals(eventData.getMethod()) || ("DELETE".equals(eventData.getMethod())))
                        && (eventData.getStatus() < 210)) {
                    exchange.setVariable("skip_line", Boolean.valueOf(false).toString().toLowerCase());
                    String message = formatMessage(eventData);
                    exchange.getMessage().setBody(message);
                } else {
                    exchange.setVariable("skip_line", Boolean.valueOf(true).toString().toLowerCase());
                }
            }

        }

        protected String formatMessage(EventData eventData) {
            String type = null;
            String api = null;
            if ("PUT".equals(eventData.getMethod())) {
                type = "Microsoft.Storage.BlobCreated";
                api = "PutBlockList";
            } else if ("DELETE".equals(eventData.getMethod())) {
                type = "Microsoft.Storage.BlobDeleted";
                api = "DeleteBlob";
            }

            return MessageFormat.format(AZURE_EVENT_TEMPLATE,
                    eventData.getSubject(),
                    type,
                    eventData.getDate().toInstant(),
                    UUID.randomUUID(),
                    api,
                    UUID.randomUUID(),
                    eventData.getUrl(),
                    0
            );
        }

    }

    class ParseLineProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            String regex = "^[\\d.]+ \\S+ \\S+ \\[([\\w:/]+\\s[+-]\\d{4})\\] \\\"(\\S+) /(\\S+)/(\\S+)/(.+?) HTTP/.{1,3}\\\" (\\d{3}) (\\S+)";
            String line = exchange.getMessage().getBody().toString();
            exchange.setVariable(EVENT_LINE_KEY, line);
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(line);
            if (matcher.find()) {
                EventData eventData = new EventData();

                String serverUrl = System.getenv().getOrDefault(CONF_AZURITE_URL, "http://localhost:100000");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(AzuriteEventFromLog.DATE_FORMAT);
                eventData.setDate(simpleDateFormat.parse(matcher.group(1)));
                eventData.setMethod(matcher.group(2));
                eventData.setAccount(matcher.group(3));
                eventData.setContainer(matcher.group(4));
                eventData.setFile("/" + URLDecoder.decode(matcher.group(5).split("\\?")[0]));
                eventData.setSubject("/" + eventData.getAccount() + "/" + eventData.container + eventData.getFile());
                eventData.setUrl(serverUrl + eventData.getSubject());
                eventData.setStatus(Integer.valueOf(matcher.group(6)));
                exchange.setVariable(EVENT_DATA_KEY, eventData);
            }

        }

    }


    class FileSkipConsumedLineProcessor implements Processor {

        @Override
        public void process(Exchange exchange) throws Exception {
            int oldPosition = 0;
            Path seekPath = Paths.get(FILE_FULL_SEEK_NAME);
            int index = Integer.valueOf(exchange.getMessage().getHeader("CamelStreamIndex").toString());
            boolean skip = false;
            if (seekPath.toFile().exists()) {
                String strOldValue = Files.readString(seekPath, StandardCharsets.UTF_8);
                oldPosition = Integer.valueOf(strOldValue);
                if (index > oldPosition) {
                    Files.writeString(seekPath, String.valueOf(index), StandardCharsets.UTF_8,
                            StandardOpenOption.WRITE);
                } else {
                    skip = true;
                }
            } else {
                oldPosition = -1;
                Files.writeString(seekPath, "0", StandardCharsets.UTF_8, StandardOpenOption.CREATE);

            }
            exchange.setVariable("old_position", oldPosition);
            exchange.setVariable("skip_line", Boolean.valueOf(skip).toString().toLowerCase());
        }
    }

    public class EventData {
        private Date date;
        private String method;
        private String account;
        private String container;
        private String file;
        private String subject;
        private String url;
        private int status;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getContainer() {
            return container;
        }

        public void setContainer(String container) {
            this.container = container;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        @Override
        public String toString() {
            return "AccessData{" +
                    "date=" + date +
                    ", method='" + method + '\'' +
                    ", account='" + account + '\'' +
                    ", container='" + container + '\'' +
                    ", file='" + file + '\'' +
                    ", url='" + url + '\'' +
                    ", status=" + status +
                    '}';
        }
    }

    private final static String AZURE_EVENT_TEMPLATE = """
            ['{'
              "source": "/subscriptions/azurite/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/my-storage-account",
              "subject": "{0}",
              "type": "{1}",
              "time": "{2}",
              "id": "{3}",
              "data": '{'
                "api": "{4}",
                "clientRequestId": "{5}",
                "requestId": "{5}",
                "eTag": "\\"{5}\\"",
                "contentType": "application/octet-stream",
                "contentLength": 0,
                "blobType": "BlockBlob",
                "url": "{6}",
                "sequencer": "{7}",
                "storageDiagnostics": '{'
                  "batchId": "{5}"
                }
              '}',
              "specversion": "1.0"
            '}']
            """;
}
