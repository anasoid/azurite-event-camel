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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AzuriteEventFromLog extends RouteBuilder {

    private final static Boolean SKIP_LINE = "true".equalsIgnoreCase(System.getenv().getOrDefault("SKIP_LINE", "true"));
    private final static LoggingLevel DEBUG_LEVEL = LoggingLevel.INFO;

    private final static String FILE_NAME = "access-azurite";
    private final static String FILE_FULL_NAME = Config.FILE_PATH + "/" + FILE_NAME + ".log";
    private final static String FILE_FULL_SEEK_NAME = Config.FILE_PATH_SEEK + "/" + FILE_NAME + ".seek";
    protected final static String DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss ZZ";
    public final static String EVENT_DATA_KEY = "event_data";
    public final static String EVENT_LINE_KEY = "event_line";


    @Override
    public void configure() throws Exception {

        from("stream:file?fileName=" + FILE_FULL_NAME + "&scanStream=true&scanStreamDelay=1000")
                .process(new FileSkipConsumedLineProcessor())
                .choice()
                .when(simple("${variable.skip_line} == '" + SKIP_LINE.toString().toLowerCase() + "'"))
                .log(DEBUG_LEVEL, "skip line : [${variable.event_line} ]")
                .otherwise()
                .to("direct:processLine")
                .endChoice();
        from("direct:processLine")
                .process(new ParseLineProcessor())
                .choice()
                .when(simple("${variable.event_data} == null "))
                .log(DEBUG_LEVEL, "skip line not access action: [ ${variable.event_line} ]")
                .otherwise()
                .to("direct:processAccessLine")
                .endChoice();
        from("direct:processAccessLine")
                .process(new AzuriteEventGeneratorProcessor())
                .choice()
                .when(simple("${variable.skip_line} == 'true' "))
                .log(DEBUG_LEVEL, "skip line not create or delete : [ ${variable.event_line} ]")
                .endChoice()
                .otherwise()
                .log(DEBUG_LEVEL, "CamelStreamIndex=${header.CamelStreamIndex}|old_position=${variable.old_position}|skip_line=${variable.skip_line} ]")
                .log(">>>> [ ${body} ]")
                .to("direct:prepareSendToBroker");
        from("direct:prepareSendToBroker")
                .process(new AzuriteEventGeneratorProcessor())
                .to("direct:sendToBroker");

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
                    String message = BodyFormater.formatBody(eventData, Config.AZURE_EVENT_FORMAT);
                    exchange.getMessage().setBody(message);
                } else {
                    exchange.setVariable("skip_line", Boolean.valueOf(true).toString().toLowerCase());
                }
            }

        }


    }

    public static class ParseLineProcessor implements Processor {
        String REGEX = "^[\\d.]+ \\S+ \\S+ \\[([\\w:/]+\\s[+-]\\d{4})\\] \\\"(\\S+) /([0-9a-zA-Z_\\-]+)/([0-9a-zA-Z_\\-]+)(/|%2F)(.+?) HTTP/.{1,3}\\\" (\\d{3}) (\\S+)";
        Pattern ACCESS_PATTERN = Pattern.compile(REGEX);

        @Override
        public void process(Exchange exchange) throws Exception {
            String line = exchange.getMessage().getBody().toString();
            exchange.setVariable(EVENT_LINE_KEY, line);
            EventData eventData = this.parse(line);
            if (eventData != null) {
                exchange.setVariable(EVENT_DATA_KEY, eventData);
            }
        }

        public EventData parse(String line) throws ParseException {


            Matcher matcher = ACCESS_PATTERN.matcher(line);
            if (matcher.find()) {
                String url = matcher.group(6);
                if (url.contains("blockid=")) {
                    return null;
                }
                if (url.contains("popreceipt=")) {
                    return null;
                }
                EventData eventData = new EventData();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(AzuriteEventFromLog.DATE_FORMAT, Locale.ENGLISH);
                eventData.setDate(simpleDateFormat.parse(matcher.group(1)));
                eventData.setMethod(matcher.group(2));
                eventData.setAccount(matcher.group(3));
                eventData.setContainer(matcher.group(4));


                eventData.setFile("/" + URLDecoder.decode(url.split("\\?")[0]));
                eventData.setSubject("/" + eventData.getAccount() + "/" + eventData.getContainer() + eventData.getFile());
                eventData.setUrl(Config.AZURITE_URL + eventData.getSubject());
                eventData.setStatus(Integer.valueOf(matcher.group(7)));
                return eventData;
            }
            return null;
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

}
