package org.anasoid.azurite.event.routes.out;


import org.anasoid.azurite.event.routes.Config;
import org.apache.camel.builder.RouteBuilder;

public class KafkaOut extends RouteBuilder {


    @Override
    public void configure() {

        from("direct:sendToKafka")
                .choice()
                .when(simple("${variable.event_data.container} == 'default' "))
                .log("to kafka ")
                .to("kafka:" + Config.DEFAULT_TOPIC + "?brokers=" + Config.BROKER + Config.KAFKA_ADDITIONAL_CONFIG)
                .endChoice()
                .otherwise()
                .log("NO TOPIC for  container : ${variable.event_data.container}")
                .endChoice();
    }


}
