package org.anasoid.azurite.event.routes;


import org.apache.camel.builder.RouteBuilder;

public class KafkaOut extends RouteBuilder {


    @Override
    public void configure() {

        from("direct:sendToKafka")
                .choice()
                .when(simple("${variable.event_data.container} == 'default' "))
                .to("kafka:" + Config.DEFAULT_TOPIC + "?brokers=" + Config.BROKER + Config.KAFKA_ADDITIONAL_CONFIG)
                .otherwise()
                .log("NO TOPIC for  container : ${variable.event_data.container}")
                .endChoice();
    }


}
