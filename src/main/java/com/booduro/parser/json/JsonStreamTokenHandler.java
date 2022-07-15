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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/**
 * @author saifeddine.benchahed
 */
public interface JsonStreamTokenHandler {

    Object processStartObject(JsonToken token, JsonParser jsonParser, Object data) throws IOException, Exception;

    Object processEndObject(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processStartArray(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processEndArray(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processFieldName(JsonToken token, JsonParser jsonParser, Object data) throws IOException, Exception;

    Object processValueEmbeddedObject(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processValueString(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processValueNumberInt(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processValueNumberFloat(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processValueTrue(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processValueFalse(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processValueNull(JsonToken token, JsonParser jsonParser, Object data) throws Exception;

    Object processNotAvailable(JsonToken token, JsonParser jsonParser, Object data) throws Exception;
}
