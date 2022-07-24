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

package com.booduro.commons.annotations;

import org.javatuples.Pair;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author saifeddine.benchahed
 */
public class AnnotatedClassVisitable {

    public final Class<?> klass;

    public static AnnotatedClassVisitable of(Class<?> klass) {
        return new AnnotatedClassVisitable(klass);
    }

    public AnnotatedClassVisitable(Class<?> klass) {
        this.klass = klass;
    }

    /**
     * returns the result of visit of all the class level annotations
     */
    public Object[] acceptClass(AnnotatedClassVisitor visitor, Object data) {
        return Arrays.stream(klass.getAnnotations())
                .map(annotation -> visitor.visit(annotation, this, Pair.with(klass, data)))
                .filter(Objects::nonNull)
                .toArray();
    }

    /**
     * returns the result of visit of all the fields level annotations
     */
    public Object[][] acceptFields(AnnotatedClassVisitor visitor, Object data) {
        Field[] schemaFields = klass.getFields();
        Object[][] fieldsVisitResults = new Object[schemaFields.length][];
        for (int i = 0; i < schemaFields.length; i++) {
            Field schemaField = schemaFields[i];
            fieldsVisitResults[i] = Arrays.stream(schemaField.getAnnotations())
                    .map(annotation -> visitor.visit(annotation, this, Pair.with(schemaField, data)))
                    .filter(Objects::nonNull)
                    .toArray();
        }
        return fieldsVisitResults;
    }
}
