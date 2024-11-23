package com.algorithms;

import com.algorithms.exceptions.DuplicateKeyException;

import java.io.Serial;
import java.io.Serializable;

public class BTree implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public BTreeNode root;
    private int t;

    public BTree(int t) {
        this.t = t;
        root = null;
    }

    public Integer search(int key) {
        return (root == null) ? null : root.sharSearch(key);
    }

    public void insert(int key, int position) {
        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = key;
            root.positions[0] = position;
            root.numberOfKeys = 1;
        }
        else {
            if (search(key) != null) {
                throw new DuplicateKeyException("Key " + key + " already exists in the B-tree.");
            }
            if (root.numberOfKeys == 2 * t - 1) {
                BTreeNode newRoot = new BTreeNode(t, false);
                newRoot.children[0] = root;
                newRoot.splitChild(0, root);

                int i = 0;

                if (newRoot.keys[0] < key)
                    i++;

                newRoot.children[i].insertNonFull(key, position);
                root = newRoot;
            } else {
                root.insertNonFull(key, position);
            }
        }
    }

    public void updatePositionsInBTree(long position, int lengthDifference) {
        if (root != null) {
            root.updatePositionsAfterChange(position, lengthDifference);
        }
    }

    public void remove(int key) {
        if (root == null) {
            return;
        }
        root.remove(key);
        if (root.numberOfKeys == 0) {
            if (root.isLeaf) {
                root = null;
            }
            else {
                root = root.children[0];
            }
        }
    }
}
