package org.anasoid.azurite.event.routes;

// camel-k: dependency=camel-azure-storage-queue
// camel-k: dependency=com.azure:azure-storage-queue:12.24.1

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.apache.camel.builder.RouteBuilder;

public class BlobQueueOut extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        // Configure the AMQP component with the connection factory
        String accountName = Config.BLOB_QUEUE_ACCOUNT_NAME;
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, Config.BLOB_QUEUE_ACCESS_KEY);
        String uri = Config.BLOB_QUEUE_ENDPOINT;//String.format("https://%s.queue.core.windows.net", "yourAccountName");

        String defaultAccountName = "devstoreaccount1";
        if (defaultAccountName.equals(accountName) && !uri.contains(defaultAccountName)) {
            if (!uri.endsWith("/")) {
                uri += "/";
            }
            uri += defaultAccountName;
        }
        QueueServiceClient client = new QueueServiceClientBuilder()
                .endpoint(uri)
                .credential(credential)
                .buildClient();
        // This is camel context
        getContext().getRegistry().bind("blobClient", client);


        from("direct:sendToBlobQueue")
                .choice()
                .when(simple("${variable.event_data.container} == 'default' "))
                .log("to BlobQueue ")
                .to("azure-storage-queue://" + accountName + "/" + "myqueue" + "?serviceClient=#blobClient&createQueue=true")
                .endChoice()
                .otherwise()
                .log("NO TOPIC for  container : ${variable.event_data.container}")
                .endChoice();

    }
}
