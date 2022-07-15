/*
 * Copyright (c) 2018 Booduro, Inc. All Rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.booduro.commons.graphs;

import com.booduro.commons.collections.Maps;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author saifeddine.benchahed
 */
public class Trees {
    /**
     * build a graphs indexing the heterogeneous tuples
     * <p>
     * grouping is done base on equality of tuples elements at the same rank
     *
     * @param tuples
     * @param indexPrefixLength
     * @return
     */
    public static Tree prefixIndexOfPolymorphicTuples(List<List<?>> tuples, int indexPrefixLength) {
        Preconditions.checkArgument(!tuples.stream().anyMatch(tuple -> tuple.size() < indexPrefixLength),
                "every tuple size should be >= index prefix length");

        Tree.Node root = Tree.newNode(null, tuples);
        doPrefixIndex(root, tuples, 0, indexPrefixLength);
        return Tree.newTree(root);
    }

    private static Tree.Node doPrefixIndex(Tree.Node root, List<List<?>> tuples, int idxStart, int idxEnd) {
        if (idxStart < idxEnd) {
            Tree.Node currentLevelIdx = oneLevelPrefixIndex(root, tuples, idxStart);
            currentLevelIdx.children.stream()
                    .forEach(node -> doPrefixIndex(node, (List<List<?>>) node.value, idxStart + 1, idxEnd));
        } else {
            root.value = tuples;
        }
        return root;
    }

    private static <E, T> Tree.Node oneLevelPrefixIndex(Tree.Node root, List<T> tuples, int indexRank) {
        Map<E, List<T>> groups = Maps.fromGroupBy(tuples, tuple -> ((List<E>) tuple).get(indexRank));
        groups.entrySet().stream()
                .forEach(entry -> {
                    Tree.Node idxNode = Tree.newNode(entry.getKey(), entry.getValue());
                    root.addChild(idxNode);
                });
        return root;
    }

    /**
     * build a graphs indexing the Homogenous tuples
     * <p>
     * grouping is done based on the grouping criteria
     *
     * @param tuples
     * @param indexPrefixLength
     * @return
     */
    public static <T, C> Tree prefixIndexOfMonomorphicTuples(List<List<T>> tuples, int indexPrefixLength, Function<T, C> groupingCriteria) {
        Objects.requireNonNull(groupingCriteria);

        Tree.Node root = Tree.newNode(null, tuples);
        doPrefixIndex(root, tuples, 0, indexPrefixLength, groupingCriteria);
        return Tree.newTree(root);
    }

    /**
     * build a graphs indexing the Homogenous tuples
     * <p>
     * grouping is done based on the grouping criteria
     *
     * @param tuples
     * @return
     */
    public static <T, C> Tree prefixIndexOfMonomorphicTuples(List<List<T>> tuples, Function<T, C> groupingCriteria) {
        return prefixIndexOfMonomorphicTuples(tuples, maxSize(tuples), groupingCriteria);
    }

    private static <T, C> Tree.Node doPrefixIndex(Tree.Node root, List<List<T>> tuples, int idxStart, int idxEnd, Function<T, C> groupingCriteria) {
        if (idxStart < idxEnd) {
            Tree.Node currentLevelIdx = oneLevelPrefixIndex(root, tuples, idxStart, groupingCriteria);
            currentLevelIdx.children.stream()
                    .forEach(node -> doPrefixIndex(node, (List<List<T>>) node.value, idxStart + 1, idxEnd, groupingCriteria));
        } else {
            root.value = tuples;
        }
        return root;
    }

    private static <T, C> Tree.Node oneLevelPrefixIndex(Tree.Node root, List<List<T>> tuples, int idxStart, Function<T, C> groupingCriteriaExtractor) {
        Map<C, List<List<T>>> criteriaToGroup = new HashMap<>();
        for (int i = 0; i < tuples.size(); i++) {
            List<T> tuple = tuples.get(i);
            if (idxStart < tuple.size()) {
                C criteria = groupingCriteriaExtractor.apply(tuple.get(idxStart));
                List<List<T>> group = criteriaToGroup.get(criteria);
                if (group == null) {
                    group = new ArrayList<>();
                    criteriaToGroup.put(criteria, group);
                }
                group.add(tuple);
            }
        }
        for (Map.Entry<C, List<List<T>>> entry: criteriaToGroup.entrySet()) {
            Tree.Node node = Tree.newNode(entry.getKey(), entry.getValue());
            root.addChild(node);
        }
        return root;
    }

    private static <T> int maxSize(List<List<T>> tuples) {
        int maxSize = 0;
        for (List<T> tuple: tuples) {
            if (tuple.size() > maxSize) {
                maxSize = tuple.size();
            }
        }
        return maxSize;
    }
}
