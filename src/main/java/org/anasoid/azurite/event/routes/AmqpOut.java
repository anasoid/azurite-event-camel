package org.anasoid.azurite.event.routes;

// camel-k: dependency=camel-amqp
// camel-k: dependency=org.apache.qpid:qpid-jms-client:2.5.0

import jakarta.jms.ConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.qpid.jms.JmsConnectionFactory;

public class AmqpOut extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Configure the AMQP component with the connection factory
        AMQPComponent amqpComponent = new AMQPComponent();
        ConnectionFactory connectionFactory = new JmsConnectionFactory(Config.AMQP_URI);
        amqpComponent.setConnectionFactory(connectionFactory);

        // Bind the AMQP component to the Camel context
        getContext().addComponent("amqp", amqpComponent);

        from("direct:sendToAmqp")
                .choice()
                .when(simple("${variable.event_data.container} == 'default' "))
                .log("to ServiceBus ")
                .to("amqp:queue:" + Config.DEFAULT_TOPIC)
                .endChoice()
                .otherwise()
                .log("NO TOPIC for  container : ${variable.event_data.container}")
                .endChoice();

    }
}
