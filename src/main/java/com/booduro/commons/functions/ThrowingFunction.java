/*
 * Copyright (c) 2018 Booduro, Inc. All Rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.booduro.commons.functions;

/**
 * @author saifeddine.benchahed
 */
@FunctionalInterface
public interface ThrowingFunction<I, O> {
    O apply(I input) throws Exception;
}
