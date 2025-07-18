package org.anasoid.azurite.event.routes;

import org.apache.camel.builder.RouteBuilder;

/*
 * Copyright 2023-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * @author : anasoid
 * Date :   4/30/25
 */public class BrokerSender extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        from("direct:sendToBroker")
                .choice()
                .when(t -> Config.IS_AMQP_TARGET)
                .to("direct:sendToAmqp").endChoice()
                .endChoice()
                .when(t -> Config.IS_KAFKA_TARGET)
                .to("direct:sendToKafka").endChoice()
                .endChoice()
                .when(t -> Config.IS_BLOB_QUEUE_TARGET)
                .to("direct:sendToBlobQueue").endChoice()
                .endChoice()
                .otherwise()
                .log("No broker configured");
    }

}
