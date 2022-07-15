/*
 * Copyright (c) 2018 Booduro, Inc. All Rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.booduro.commons.graphs;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree data structure hu
 * @author saifeddine.benchahed
 */
public class Tree {
    public final Node root;

    public Tree(Node root) {
        this.root = root;
    }

    /**
     * creates a new tree
     * @param root
     * @return
     */
    public static Tree newTree(Node root) {
        return new Tree(root);
    }

    public static Node newNode(Object key, Object value) {
        return new Node(key, value);
    }

    public static class Node implements TreeNodeKeyVisitable {
        public Object key;
        public Object value;
        public Node parent;
        public List<Node> children = new ArrayList<>();

        public Node(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public Node addChild(Node child) {
            child.parent = this;
            this.children.add(child);
            return this;
        }

        public Node addChildren(List<Node> children) {
            for (Node child: children) {
                addChild(child);
            }
            return this;
        }

        @Override
        public Object accept(TreeNodeKeyVisitor visitor, Object data) {
            return visitor.visit(this.key, this, data);
        }

        @Override
        public Object[] acceptChildren(TreeNodeKeyVisitor visitor, Object data) {
            return children.stream().map(childNode -> visitor.visit(childNode.key, childNode, data)).toArray();
        }

        @Override
        public String toString() {
            return "{" + key + "; " + value + "}";
        }
    }
}
