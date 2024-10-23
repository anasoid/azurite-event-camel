package org.anasoid.azurite.event.test;

import org.apache.camel.builder.RouteBuilder;

public class KafkaInTesting extends RouteBuilder {


    private final static String CONF_DEFAULT_TOPIC = "DEFAULT_TOPIC";
    private final static String DEFAULT_TOPIC = System.getenv().getOrDefault(CONF_DEFAULT_TOPIC, "azurite");

    private final static String CONF_KAFKA_BROKER = "KAFKA_BROKER";
    private final static String BROKER = System.getenv().getOrDefault(CONF_KAFKA_BROKER, "localhost:9092");


    @Override
    public void configure() throws Exception {
        from("kafka:" + DEFAULT_TOPIC + "?brokers=" + BROKER)
                .log(">>>>From kafka : ${body}");
    }


}
