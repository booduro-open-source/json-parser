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

import com.booduro.commons.collections.Table;
import com.booduro.commons.functions.ThrowingFunction;
import com.booduro.commons.graphs.Tree;
import com.booduro.parser.json.pathtoken.Token;
import com.booduro.parser.json.pathtoken.Wildcard;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.ImmutableList;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * @author saifeddine.benchahed
 */
public class JsonStreamParser {
    //private static AllPathsTreeFieldsValuesExtractor allPathsTreeFieldsValuesExtractor = new AllPathsTreeFieldsValuesExtractor();
    static boolean logEnabled = false;

    public static <T> List<T> parse(InputStream json, Class<T> schema) {
        try {
            Tree allPathsTree = JsonPaths.allPathsTree(schema);
            Deque tuplesAccumulator = new ArrayDeque<>();
            collectData(json, allPathsTree, tuplesAccumulator);
            return buildEntitiesFromAllPathsTreeData((Table) ((List) tuplesAccumulator.poll()).get(0), schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Table parseAsTable(InputStream json, Class<T> schema) {
        try {
            Tree allPathsTree = JsonPaths.allPathsTree(schema);
            Deque tuplesAccumulator = new ArrayDeque<>();
            collectData(json, allPathsTree, tuplesAccumulator);
            return (Table) ((List) tuplesAccumulator.poll()).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> List<T> buildEntitiesFromAllPathsTreeData(Table table, Class<T> schema) throws IllegalAccessException, InstantiationException {
        List<T> entities = new ArrayList<>();
        Object[] fields = table.headers;
        for (Object[] row: table.rows) {
            T entity = schema.newInstance();
            for (int i = 0; i < fields.length; i++) {
                Field field = (Field) fields[i];
                assignValue(entity, field, row[i]);
            }
            entities.add(entity);
        }
        return entities;
    }

    private static <T> void assignValue(T entity, Field field, Object value) throws IllegalAccessException {
        String fieldType = field.getType().getSimpleName();
        switch (fieldType) {
            case "int":
                if (value != null) {
                    if (value instanceof Number) {
                        field.setInt(entity, ((Number) value).intValue());
                    } else {
                        throw new RuntimeException("type " + int.class + " is not assignable from " + value.getClass());
                    }
                }
                break;
            case "float":
                if (value != null) {
                    if (value instanceof Number) {
                        field.setFloat(entity, ((Number) value).floatValue());
                    } else {
                        throw new RuntimeException("type " + float.class + " is not assignable from " + value.getClass());
                    }
                }
                break;
            case "boolean":
                if (value != null) {
                    if (value instanceof Boolean) {
                        field.setBoolean(entity, (Boolean) value);
                    } else {
                        throw new RuntimeException("type " + boolean.class + " is not assignable from " + value.getClass());
                    }
                }
                break;
            case "Integer":
                if (value != null) {
                    if (value instanceof Number) {
                        field.set(entity, ((Number) value).intValue());
                    } else {
                        throw new RuntimeException("type " + Integer.class + " is not assignable from " + value.getClass());
                    }
                }
                break;
            case "Float":
                if (value != null) {
                    if (value instanceof Number) {
                        field.set(entity, ((Number) value).floatValue());
                    } else {
                        throw new RuntimeException("type " + Float.class + " is not assignable from " + value.getClass());
                    }
                }
                break;
            case "Boolean":
                if (value != null) {
                    if (value instanceof Boolean) {
                        field.set(entity, value);
                    } else {
                        throw new RuntimeException("type " + Boolean.class + " is not assignable from " + value.getClass());
                    }
                }
                break;
            case "String":
                if (value != null) {
                    if (value instanceof String) {
                        field.set(entity, value);
                    } else if (value.getClass().isPrimitive()) {
                        field.set(entity, "" + value);
                    } else {
                        field.set(entity, value.toString());
                    }
                }
                break;
            default:
                throw new RuntimeException("field type " + fieldType + " not supported");
        }
    }

    private static void collectData(InputStream json, Tree allPathsTree, Deque tuplesAccumulator) throws Exception {
        JsonStreamProcessor.process(json, new JsonPathBasedDataExtractor(tuplesAccumulator), Pair.with(allPathsTree.root, null));
    }

    public static class JsonPathBasedDataExtractor implements JsonStreamTokenHandler {
        private static Logger logger = LoggerFactory.getLogger(JsonPathBasedDataExtractor.class);
        private Deque<Pair<Tree.Node, JsonToken>> openingBracketsPreviousSettings = new ArrayDeque<>();
        private Deque openingBracketsMarkedParsedTables = new ArrayDeque<>();

        public JsonPathBasedDataExtractor(Deque tuplesAccumulator) {
            openingBracketsMarkedParsedTables = tuplesAccumulator;
        }

        @Override
        public Object processStartObject(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            openingBracketsPreviousSettings.push((Pair<Tree.Node, JsonToken>) data);
            openingBracketsMarkedParsedTables.push("{");
            if (logEnabled) {
                logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), ((Pair<Tree.Node, ?>) data).getValue0().key);
            }
            return data;
        }

        @Override
        public Object processEndObject(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            Pair<Tree.Node, JsonToken> openingBracketPreviousSettings = openingBracketsPreviousSettings.poll();
            Tree.Node objectPreviousMatch = openingBracketPreviousSettings.getValue0();
            JsonToken objectPreviousToken = openingBracketPreviousSettings.getValue1();
            if (logEnabled) {
                logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), objectPreviousMatch.key);
            }
            Tree.Node nextMatcherParent = null;
            if (JsonToken.FIELD_NAME.equals(objectPreviousToken)) {
                nextMatcherParent = objectPreviousMatch.parent;
            } else {
                nextMatcherParent = objectPreviousMatch;
            }
            //collect parsed tuples
            Object elt = openingBracketsMarkedParsedTables.poll();
            List<List<Table>> collectedTables = new ArrayList<>();
            boolean merge = false;
            while (!(elt instanceof String && elt.equals("{"))) {
                if (elt instanceof String && elt.equals("*")) {
                    merge = true;
                } else {
                    collectedTables.add((List<Table>) elt);
                }
                elt = openingBracketsMarkedParsedTables.poll();
            }
            if (collectedTables.size() > 0) {
                List<Table> combination = merge ? ImmutableList.of(Table.mergeAll(collectedTables)) : Table.product(collectedTables);
                openingBracketsMarkedParsedTables.push(combination);
            }
            return Pair.with(nextMatcherParent, token);
        }

        @Override
        public Object processStartArray(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            if (logEnabled) {
                logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), ((Pair<Tree.Node, ?>) data).getValue0().key);
            }
            openingBracketsPreviousSettings.push((Pair<Tree.Node, JsonToken>) data);
            openingBracketsMarkedParsedTables.push("[");
            Tree.Node previousMatch = ((Pair<Tree.Node, ?>) data).getValue0();
            Tree.Node currentMatch = nextMatch(previousMatch, "*");
            return Pair.with(currentMatch, token);
        }

        private static Tree.Node nextMatch(Tree.Node matchedNode, String literal) {
            Tree.Node nextMatch = null;
            Tree.Node noMatch = null;
            int matches = 0;
            for (int i = 0; i < matchedNode.children.size(); i++) {
                Tree.Node child = matchedNode.children.get(i);
                if (child.key instanceof Token && (((Token) child.key).literal.equals(literal) || ((Token) child.key).literal.equals("*"))) {
                    matches++;
                    nextMatch = child;
                } else if (child.key instanceof String && ((String) child.key).startsWith("#")) {
                    noMatch = child;
                }
            }
            if (matches > 1) {
                throw new JsonParsingException("Many matches found");
            } else if (matches == 0) {
                nextMatch = noMatch;
            }
            return nextMatch;
        }

        @Override
        public Object processEndArray(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            Pair<Tree.Node, JsonToken> openingBracketPreviousSettings = openingBracketsPreviousSettings.poll();
            Tree.Node previousMatch = openingBracketPreviousSettings.getValue0();
            JsonToken previousToken = openingBracketPreviousSettings.getValue1();
            if (logEnabled) {
                logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), previousMatch.key);
            }

            Tree.Node nextMatcherParent = null;
            if (JsonToken.FIELD_NAME.equals(previousToken)) {//parent is object
                nextMatcherParent = previousMatch.parent;
            } else {
                nextMatcherParent = previousMatch;//parent is array
            }
            //collect parsed tuples
            Object elt = openingBracketsMarkedParsedTables.poll();
            List<List<Table>> collectedTables = new ArrayList<>();
            while (!(elt instanceof String && elt.equals("["))) {
                collectedTables.add((List<Table>) elt);
                elt = openingBracketsMarkedParsedTables.poll();
            }
            if (collectedTables.size() > 0) {
                openingBracketsMarkedParsedTables.push(ImmutableList.of(Table.mergeAll(collectedTables)));
            }
            return Pair.with(nextMatcherParent, token);
        }

        @Override
        public Object processFieldName(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            if (logEnabled) {
                logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), ((Pair<Tree.Node, ?>) data).getValue0().key);
            }
            Tree.Node previousMatch = ((Pair<Tree.Node, ?>) data).getValue0();
            Tree.Node currentMatch = nextMatch(previousMatch, jsonParser.getText());
            if (currentMatch.key instanceof Wildcard) {
                openingBracketsMarkedParsedTables.push("*");
            }
            return Pair.with(currentMatch, token);
        }

        @Override
        public Object processValueEmbeddedObject(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            if (logEnabled) {
                logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), ((Pair<Tree.Node, ?>) data).getValue0().key);
            }
            throw new IllegalArgumentException("non supported value");
        }

        @Override
        public Object processValueString(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            return processValue(token, jsonParser, data, jp -> jp.getValueAsString());
        }

        private Object processValue(JsonToken token,
                                    JsonParser jsonParser,
                                    Object data,
                                    ThrowingFunction<JsonParser, Object> jsonParserValueExtractor) throws Exception {
            if (logEnabled) {
                logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), ((Pair<Tree.Node, ?>) data).getValue0().key);
            }
            Tree.Node previousMatch = ((Pair<Tree.Node, ?>) data).getValue0();
            if (previousMatch.children != null && previousMatch.children.size() > 0) {
                Tree.Node currentMatch = previousMatch.children.get(0);

                if (currentMatch.key instanceof Field) {
                    Field field = (Field) currentMatch.key;
                    Field[] headers = {field};
                    Object[] row = {jsonParserValueExtractor.apply(jsonParser)};
                    openingBracketsMarkedParsedTables.push(ImmutableList.of(Table.table(headers, ImmutableList.of(row))));
                }

                JsonToken previousToken = ((Pair<?, JsonToken>) data).getValue1();
                Tree.Node nextMatcherParent = null;
                if (previousToken.equals(JsonToken.FIELD_NAME)) {
                    nextMatcherParent = currentMatch.parent.parent;// son of the object
                } else {//son of the array
                    nextMatcherParent = currentMatch.parent;
                }
                return Pair.with(nextMatcherParent, token);
            }
            return data;
        }

        @Override
        public Object processValueNumberInt(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            return processValue(token, jsonParser, data, jp -> jp.getIntValue());
        }

        @Override
        public Object processValueNumberFloat(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            return processValue(token, jsonParser, data, jp -> jp.getFloatValue());
        }

        @Override
        public Object processValueTrue(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            return processValue(token, jsonParser, data, jp -> jp.getBooleanValue());
        }

        @Override
        public Object processValueFalse(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            return processValue(token, jsonParser, data, jp -> jp.getBooleanValue());
        }

        @Override
        public Object processValueNull(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            return processValue(token, jsonParser, data, jp -> null);
        }

        @Override
        public Object processNotAvailable(JsonToken token, JsonParser jsonParser, Object data) throws Exception {
            //logger.info("processing json token {} : {} with {} matcher", token, jsonParser.getText(), ((Pair<Tree.Node, ?>) data).getValue0().key);
            throw new IllegalArgumentException("non supported value");
        }
    }
}
