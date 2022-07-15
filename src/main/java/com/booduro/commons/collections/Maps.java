/*
 * Copyright (c) 2018 Booduro, Inc. All Rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.booduro.commons.collections;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author saifeddine.benchahed
 */
public class Maps {
    public static <T, E> Map<T, List<E>> fromGroupBy(List<E> elements, Function<E, T> groupingCriteria) {
        return elements.stream()
                .collect(Collectors.groupingBy(groupingCriteria));
    }
}
