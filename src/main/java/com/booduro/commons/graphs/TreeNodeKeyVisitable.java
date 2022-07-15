/*
 * Copyright (c) 2018 Booduro, Inc. All Rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.booduro.commons.graphs;

/**
 * @author saifeddine.benchahed
 */
public interface TreeNodeKeyVisitable {
    Object accept(TreeNodeKeyVisitor visitor, Object data);

    Object[] acceptChildren(TreeNodeKeyVisitor visitor, Object data);
}
