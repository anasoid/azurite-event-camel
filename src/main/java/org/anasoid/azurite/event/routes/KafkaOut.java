package org.anasoid.azurite.event.routes;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import static org.anasoid.azurite.event.routes.AzuriteEventFromLog.EVENT_DATA_KEY;

public class KafkaOut extends RouteBuilder {


    @Override
    public void configure() {
        from("direct:sendToKafka")
                .process(new PrepareKafka())
                .to("kafka:" + Config.DEFAULT_TOPIC + "?brokers=" + Config.BROKER + Config.KAFKA_ADDITIONAL_CONFIG);
    }

    /**
     *
     */
    class PrepareKafka implements Processor {
        @Override
        public void process(Exchange exchange) {
            EventData eventData = (EventData) exchange.getVariable(EVENT_DATA_KEY);
            // HOOK
        }


    }


}
