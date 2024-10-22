package org.anasoid.azuriteevent;

import org.anasoid.azuriteevent.AzuriteEventFromLog.EventData;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import java.util.Map;

import static org.anasoid.azuriteevent.AzuriteEventFromLog.EVENT_DATA_KEY;

public class KafkaOut extends RouteBuilder {

    private final static Map<String, String> TOPICS = Map.of();
    private final static String CONF_DEFAULT_TOPIC = "DEFAULT_TOPIC";
    private final static String DEFAULT_TOPIC = System.getenv().getOrDefault(CONF_DEFAULT_TOPIC, "azurite");
    ;
    private final static String CONF_BROKER = "BROKER";
    private final static String BROKER = System.getenv().getOrDefault(CONF_BROKER, "localhost:9092");
    ;

    @Override
    public void configure() throws Exception {


        from("direct:sendToKafka")
                .process(new PrepareKafka())
                .to("kafka:" + DEFAULT_TOPIC + "?brokers=" + BROKER);
    }

    /**
     *
     */
    class PrepareKafka implements Processor {
        @Override
        public void process(Exchange exchange) {
            EventData eventData = (EventData) exchange.getVariable(EVENT_DATA_KEY);
            exchange.setVariable("kafka.topic", getTopic(eventData));
            exchange.getIn().setHeader("kafka.topic", getTopic(eventData));
        }


        private String getTopic(EventData eventData) {
            return TOPICS.getOrDefault(eventData.getContainer(), DEFAULT_TOPIC);
        }
    }


}
