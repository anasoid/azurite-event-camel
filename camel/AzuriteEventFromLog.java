package camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AzuriteEventFromLog extends RouteBuilder {

    private final static String FILE_NAME = "access-azurite";
    private final static String FILE_PATH = "./compose/.logs";
    private final static String FILE_FULL_NAME = FILE_PATH + "/" + FILE_NAME + ".log";
    private final static String FILE_FULL_SEEK_NAME = FILE_PATH + "/" + FILE_NAME + ".seek";

    @Override
    public void configure() throws Exception {
        from("stream:file?fileName=" + FILE_FULL_NAME + "&scanStream=true&scanStreamDelay=1000")
                .process(new Processor() {

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

                })
                .to("direct:processLine");

        from("direct:processLine")
                .process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String regex = "^[\\d.]+ \\S+ \\S+ \\[([\\w:/]+\\s[+-]\\d{4})\\] \\\"(\\S+) /(\\S+)/(\\S+)/(.+?) HTTP/.{1,3}\\\" (\\d{3}) (\\S+)";
                        String line = exchange.getMessage().getBody().toString();
                        System.out.println("Apache log input line: " + line);
                        Pattern p = Pattern.compile(regex);
                        Matcher matcher = p.matcher(line);
                        if (matcher.find()) {
                            System.out.println(">> Date/Time: " + matcher.group(1));
                            System.out.println(">> method: " + matcher.group(2));
                            System.out.println(">> account: " + matcher.group(3));
                            System.out.println(">> container: " + matcher.group(4));
                            System.out.println(">> file: " + matcher.group(5).split("\\?")[0]);
                            System.out.println(">> url: " + matcher.group(6));
                            System.out.println(">> status: " + matcher.group(6));
                        }

                    }

                })
                .log("<<<<${header.CamelStreamIndex}|${variable.old_position}|${variable.skip_line}>>>> ${body}");
    }
}
