package org.anasoid.azurite.event.routes;


import org.anasoid.azurite.event.routes.AzuriteEventFromLog.ParseLineProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/*
 * Copyright 2023-2024 the original author or authors.
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
 * Date :   10/22/24
 */

class AzuriteEventFromLogTest {

    @Test
    void parseDate() throws ParseException {

        String date = "17/Oct/2024:16:18:21 +0100";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(AzuriteEventFromLog.DATE_FORMAT);
        Assertions.assertEquals(date, simpleDateFormat.format(simpleDateFormat.parse(date)));
    }


    @Test
    void parseLines() throws ParseException {
        String line = "192.168.240.1 - - [10/Mar/2025:15:02:01 +0000] \"PUT /devstoreaccount1/referential/referential_category0.xml HTTP/1.1\" 201 -";
        String account = "devstoreaccount1";
        String file = "/referential_category0.xml";
        ParseLineProcessor parseLineProcessor = new ParseLineProcessor();
        EventData eventData = parseLineProcessor.parse(line);
        Assertions.assertNotNull(eventData);
        Assertions.assertEquals(eventData.getAccount(), account);
        Assertions.assertEquals(eventData.getContainer(), "referential");
        Assertions.assertEquals(eventData.getMethod(), "PUT");
        Assertions.assertEquals(eventData.getFile(), file);
        Assertions.assertEquals(eventData.getStatus(), 201);
    }

    @Test
    void parseDeleteLine() throws ParseException {
        String line = "192.168.32.1 - - [30/Apr/2025:19:16:14 +0000] \"DELETE /devstoreaccount1/default/messages/f047d33d-4da9-4592-8a11-6b2c5bad521d?popreceipt=MzBBcHIyMDI1MTk6MTY6MTQ1OTNl&timeout=30 HTTP/1.1\" 204 -";
        String account = "devstoreaccount1";
        String file = "/messages/f047d33d-4da9-4592-8a11-6b2c5bad521d";
        ParseLineProcessor parseLineProcessor = new ParseLineProcessor();
        EventData eventData = parseLineProcessor.parse(line);
        Assertions.assertNotNull(eventData);
        Assertions.assertEquals(eventData.getAccount(), account);
        Assertions.assertEquals(eventData.getContainer(), "default");
        Assertions.assertEquals(eventData.getMethod(), "DELETE");
        Assertions.assertEquals(eventData.getFile(), file);
        Assertions.assertEquals(eventData.getStatus(), 204);
    }

    @Test
    void parseDeleteLineMultiFodler() throws ParseException {
        String line = "192.168.32.1 - - [30/Apr/2025:19:14:37 +0000] \"DELETE /devstoreaccount1/default/messages%2Fmessages%2F52f68323-8861-4935-aa9a-928696113ca2?se=2025-05-30T19%3A14%3A35Z&sig=l7aJ0dJWI6RD%2FZxW8b3SXGm%2BHBH0KAFXkHd5e7grmYc%3D&sp=rdl&sr=c&sv=2018-03-28 HTTP/1.1\" 202 -";
        String account = "devstoreaccount1";
        String file = "/messages/messages/52f68323-8861-4935-aa9a-928696113ca2";
        ParseLineProcessor parseLineProcessor = new ParseLineProcessor();
        EventData eventData = parseLineProcessor.parse(line);
        Assertions.assertNotNull(eventData);
        Assertions.assertEquals(eventData.getAccount(), account);
        Assertions.assertEquals(eventData.getContainer(), "default");
        Assertions.assertEquals(eventData.getMethod(), "DELETE");
        Assertions.assertEquals(eventData.getFile(), file);
        Assertions.assertEquals(eventData.getStatus(), 202);
    }

    @Test
    void parseDeleteMessage() throws ParseException {
        String line = "192.168.32.1 - - [30/Apr/2025:19:16:14 +0000] \"DELETE /devstoreaccount1/default/messages/f047d33d-4da9-4592-8a11-6b2c5bad521d?popreceipt=MzBBcHIyMDI1MTk6MTY6MTQ1OTNl&timeout=30 HTTP/1.1\" 204 -";
        String account = "devstoreaccount1";
        String file = "/messages/f047d33d-4da9-4592-8a11-6b2c5bad521d";
        ParseLineProcessor parseLineProcessor = new ParseLineProcessor();
        EventData eventData = parseLineProcessor.parse(line);
        Assertions.assertNotNull(eventData);
        Assertions.assertEquals(eventData.getAccount(), account);
        Assertions.assertEquals(eventData.getContainer(), "default");
        Assertions.assertEquals(eventData.getMethod(), "DELETE");
        Assertions.assertEquals(eventData.getFile(), file);
        Assertions.assertEquals(eventData.getStatus(), 204);
    }

}