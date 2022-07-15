/*
 * Copyright (c) 2018 Booduro, Inc. All Rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.booduro.commons.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author saifeddine.benchahed
 */
public abstract class AnnotatedClassVisitor {

    public Object visit(Annotation annotation, AnnotatedClassVisitable visitable, Object data) {
        try {
            //annotationClass(annotation);
            Method downPolymorphic = this.getClass().getMethod("visit",
                    new Class[]{annotation.annotationType(), AnnotatedClassVisitable.class, Object.class});

            if (downPolymorphic == null) {
                return defaultVisit(annotation);
            } else {
                return downPolymorphic.invoke(this, annotation, visitable, data);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return this.defaultVisit(annotation);//TODO fix this as some exceptions may be hidden
        }
    }

    protected Object defaultVisit(Object object) {
        return null;
    }
}
