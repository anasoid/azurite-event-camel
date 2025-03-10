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
        Assertions.assertEquals(eventData.getAccount(),account);
        Assertions.assertEquals(eventData.getFile(),file);
    }


}