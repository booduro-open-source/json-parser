/*
 * Copyright (c) 2018 Booduro, Inc. All Rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
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
