package org.anasoid.azurite.event.routes;

import java.text.MessageFormat;
import java.util.UUID;

public class BodyFormater {


    public final static String formatBody(EventData eventData, String format) {
        String type = null;
        String api = null;
        if ("PUT".equals(eventData.getMethod())) {
            type = "Microsoft.Storage.BlobCreated";
            api = "PutBlockList";
        } else if ("DELETE".equals(eventData.getMethod())) {
            type = "Microsoft.Storage.BlobDeleted";
            api = "DeleteBlob";
        }

        String template;
        if ("grid".equalsIgnoreCase(format)) {
            template = AZURE_GRID_EVENT_TEMPLATE;
        } else if ("cloud".equalsIgnoreCase(format)) {
            template = AZURE_CLOUD_EVENT_TEMPLATE;
        } else {
            throw new IllegalArgumentException("Invalid format " + format);
        }

        return MessageFormat.format(template,
                eventData.getSubject(),
                type,
                eventData.getDate().toInstant(),
                UUID.randomUUID(),
                api,
                UUID.randomUUID(),
                eventData.getUrl(),
                0
        );
    }

    private final static String AZURE_CLOUD_EVENT_TEMPLATE = """
            ['{'
              "source": "/subscriptions/azurite/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/my-storage-account",
              "subject": "{0}",
              "type": "{1}",
              "time": "{2}",
              "id": "{3}",
              "data": '{'
                "api": "{4}",
                "clientRequestId": "{5}",
                "requestId": "{5}",
                "eTag": "\\"{5}\\"",
                "contentType": "application/octet-stream",
                "contentLength": 0,
                "blobType": "BlockBlob",
                "url": "{6}",
                "sequencer": "{7}",
                "storageDiagnostics": '{'
                  "batchId": "{5}"
                }
              '}',
              "specversion": "1.0"
            '}']
            """;

    private final static String AZURE_GRID_EVENT_TEMPLATE = """
            ['{'
              "topic": "/subscriptions/azurite/resourceGroups/Storage/providers/Microsoft.Storage/storageAccounts/my-storage-account",
              "subject": "{0}",
              "eventType": "{1}",
              "eventTime": "{2}",
              "id": "{3}",
              "data": '{'
                "api": "{4}",
                "clientRequestId": "{5}",
                "requestId": "{5}",
                "eTag": "\\"{5}\\"",
                "contentType": "application/octet-stream",
                "contentLength": 0,
                "blobType": "BlockBlob",
                "url": "{6}",
                "sequencer": "{7}",
                "storageDiagnostics": '{'
                  "batchId": "{5}"
                }
              '}',
               "dataVersion": "",
               "metadataVersion": "1"
            '}']
            """;
}
