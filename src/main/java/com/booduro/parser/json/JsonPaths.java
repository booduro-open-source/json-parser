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

import com.booduro.commons.annotations.AnnotatedClassVisitable;
import com.booduro.commons.annotations.AnnotatedClassVisitor;
import com.booduro.commons.graphs.Tree;
import com.booduro.commons.graphs.Trees;
import com.booduro.parser.json.pathtoken.Token;
import org.javatuples.Pair;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author saifeddine.benchahed
 */
public class JsonPaths {

    private static Map<Class<?>, Tree> schemaToJsonPaths = new ConcurrentHashMap<>();
    private static JsonPathsBuilder jsonPathsBuilder = new JsonPathsBuilder();

    public static Tree allPathsTree(Class<?> schema) {
        return schemaToJsonPaths.computeIfAbsent(schema, klass -> {
                    List<List<PathSegment>> allPaths = Arrays.stream(AnnotatedClassVisitable.of(schema).acceptFields(jsonPathsBuilder, null))
                            .map(fieldVisitResult -> {
                                if (fieldVisitResult != null && fieldVisitResult.length > 0) {
                                    return (List<PathSegment>) fieldVisitResult[0];
                                } else {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return Tree.newTree(
                            addNoMatchNodes(
                                    setKeysToTokensAndValuesToNulls(
                                            allPathsTreeFromPrefixIndex(
                                                    Trees.prefixIndexOfMonomorphicTuples(allPaths,
                                                            pathSegment -> Pair.with(pathSegment.index, pathSegment.token)).root.children.get(0)))));
                }
        );
    }

    private static Tree.Node setKeysToTokensAndValuesToNulls(Tree.Node root) {
        root.parent = null;
        //DFS
        Deque<Tree.Node> toVisit = new ArrayDeque<>();
        toVisit.push(root);
        while (toVisit.peekFirst() != null) {
            Tree.Node currentNode = toVisit.pollFirst();
            if (currentNode.key instanceof Pair) {
                currentNode.key = ((Pair<?, Token>) currentNode.key).getValue1();
            }
            currentNode.value = null;
            for (Tree.Node child: currentNode.children) {
                toVisit.add(child);
            }
        }
        return root;
    }

    private static Tree.Node addNoMatchNodes(Tree.Node root) {
        int maxDepth = 50;//instead of maxDepth(root) in order to match deeper json
        return doAddNoMatchNodes(root, maxDepth);
    }

    private static Tree.Node doAddNoMatchNodes(Tree.Node root, int maxDepth) {
        Tree.Node noMatch = Tree.newNode("#", null);
        root.addChild(noMatch);
        for (int i = 0; i < maxDepth - 2; i++) {
            Tree.Node newChild = Tree.newNode("#", null);
            noMatch.addChild(newChild);
            noMatch = newChild;
        }
        if (root.children.size() > 0) {
            root.children.stream().filter(child -> !(child.key instanceof String && ((String) child.key).startsWith("#")))
                    .forEach(child -> doAddNoMatchNodes(child, maxDepth - 1));
        }
        return root;
    }

    private static Tree.Node allPathsTreeFromPrefixIndex(Tree.Node root) {
        if (root.children.size() > 0) {
            for (int i = 0; i < root.children.size(); i++) {
                root.value = null;
                root.children.set(i, allPathsTreeFromPrefixIndex(root.children.get(i)));
            }
        } else {
            List<List<PathSegment>> branches = (List<List<PathSegment>>) root.value;
            for (int i = 0; i < branches.size(); i++) {
                List<PathSegment> pathSegments = branches.get(i);
                Tree.Node remainingPath = root;
                int remainingPathStart = ((Pair<Integer, PathSegment>) root.key).getValue0() + 1;
                while (remainingPathStart < pathSegments.size()) {
                    Tree.Node nextNode = Tree.newNode(
                            Pair.with(remainingPathStart, pathSegments.get(remainingPathStart).token),
                            null);
                    remainingPath.addChild(nextNode);
                    remainingPath = nextNode;
                    remainingPathStart++;
                }
                remainingPath.addChild(Tree.newNode(pathSegments.get(pathSegments.size() - 1).field, null));
            }
        }
        return root;
    }

    private static int shortestPathLength(List<List<PathSegment>> allPaths) {
        int shortestPathLength = Integer.MAX_VALUE;
        for (int i = 0; i < allPaths.size(); i++) {
            int currentPathLength = allPaths.get(i).size();
            if (currentPathLength < shortestPathLength) {
                shortestPathLength = currentPathLength;
            }
        }
        return shortestPathLength;
    }

    public static class PathSegment {
        public final Field field;
        public final Token token;
        public final int index;

        public PathSegment(Field field, Token token, int index) {
            this.field = field;
            this.token = token;
            this.index = index;
        }
    }

    public static class JsonPathsBuilder extends AnnotatedClassVisitor {

        public Object visit(JsonPath annotation, AnnotatedClassVisitable visitable, Object data) {
            Field field = ((Pair<Field, Object>) data).getValue0();
            List<Token> path = Token.fullPath(annotation.value());
            List<PathSegment> decoratedPath = new ArrayList<>();
            for (int i = 0; i < path.size(); i++) {
                decoratedPath.add(new PathSegment(field, path.get(i), i));
            }
            return decoratedPath;
        }
    }
}
