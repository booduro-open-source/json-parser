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

package com.booduro.commons.graphs;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author saifeddine.benchahed
 */
public abstract class TreeNodeKeyVisitor {
    public Object visit(Object nodeKey, Tree.Node node, Object data) {
        try {
            Method downPolymorphic = this.getClass().getMethod("visit",
                    new Class[]{resolveClass(nodeKey), Tree.Node.class, Object.class});

            if (downPolymorphic == null) {
                return defaultVisit(nodeKey, node, data);
            } else {
                return downPolymorphic.invoke(this, new Object[]{nodeKey, node, data});
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return this.defaultVisit(nodeKey, node, data);
        }
    }

    private Class<?> resolveClass(Object nodeKey) {
        if (nodeKey instanceof Annotation) {
            return ((Annotation) nodeKey).annotationType();
        }
        return nodeKey.getClass();
    }

    protected Object defaultVisit(Object object, Tree.Node node, Object data) {
        return null;
    }
}
