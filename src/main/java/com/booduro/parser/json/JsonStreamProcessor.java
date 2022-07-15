/*
 * Copyright 2018 BOODURO inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.booduro.parser.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author saifeddine.benchahed
 */
public class JsonStreamProcessor {
    private static JsonFactory jsonFactory = new JsonFactory();

    public static void process(InputStream json, JsonStreamTokenHandler handler, Object settings) throws Exception {
        JsonParser parser = jsonFactory.createParser(json);
        try {
            JsonToken token = parser.nextToken();
            while (token != null) {
                switch (token) {
                    case NOT_AVAILABLE:
                        settings = handler.processNotAvailable(token, parser, settings);
                        break;
                    case START_OBJECT:
                        settings = handler.processStartObject(token, parser, settings);
                        break;
                    case END_OBJECT:
                        settings = handler.processEndObject(token, parser, settings);
                        break;
                    case START_ARRAY:
                        settings = handler.processStartArray(token, parser, settings);
                        break;
                    case END_ARRAY:
                        settings = handler.processEndArray(token, parser, settings);
                        break;
                    case FIELD_NAME:
                        settings = handler.processFieldName(token, parser, settings);
                        break;
                    case VALUE_EMBEDDED_OBJECT:
                        settings = handler.processValueEmbeddedObject(token, parser, settings);
                        break;
                    case VALUE_STRING:
                        settings = handler.processValueString(token, parser, settings);
                        break;
                    case VALUE_NUMBER_INT:
                        settings = handler.processValueNumberInt(token, parser, settings);
                        break;
                    case VALUE_NUMBER_FLOAT:
                        settings = handler.processValueNumberFloat(token, parser, settings);
                        break;
                    case VALUE_TRUE:
                        settings = handler.processValueTrue(token, parser, settings);
                        break;
                    case VALUE_FALSE:
                        settings = handler.processValueFalse(token, parser, settings);
                        break;
                    case VALUE_NULL:
                        settings = handler.processValueNull(token, parser, settings);
                        break;
                }
                token = parser.nextToken();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
