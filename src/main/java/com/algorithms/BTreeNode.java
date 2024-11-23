package com.algorithms;

import com.algorithms.counters.ComparisonCounter;
import com.algorithms.exceptions.DuplicateKeyException;

import java.io.Serial;
import java.io.Serializable;

public class BTreeNode implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public int[] keys;
    public int[] positions;
    int t;
    public BTreeNode[] children;
    public int numberOfKeys;
    public boolean isLeaf;

    public BTreeNode(int t, boolean isLeaf) {
        this.t = t;
        this.isLeaf = isLeaf;
        keys = new int[2 * t - 1];
        positions = new int[2 * t - 1];
        children = new BTreeNode[2 * t];
        numberOfKeys = 0;
    }

    public Integer sharSearch(int key, ComparisonCounter comparisonCount) {
        int actualLength = findActualLength(keys);
        int k = (int) (Math.log(actualLength) / Math.log(2));
        int i = (int) Math.pow(2, k);
        int currentKey = keys[i - 1];
        int delta = -1, l, counter = 0;
        comparisonCount.increment();
        if (currentKey == key) {
            return positions[i - 1];
        }
        else if (currentKey < key && actualLength > Math.pow(2, k)) {
            l = (int) (Math.log(actualLength - Math.pow(2, k) + 1) / Math.log(2));
            i = actualLength + 1 - (int) Math.pow(2, l);
            while (i > 0 && i <= actualLength) {
                counter++;
                currentKey = keys[i - 1];
                comparisonCount.increment();
                if (currentKey == keys[actualLength - 1] && key > currentKey) {
                    i += 1;
                    break;
                }
                if (delta == 0 && currentKey > key && keys[i - 2] < key) {
                    break;
                }
                if (delta == 0 && currentKey < key && keys[i] > key) {
                    i += 1;
                    break;
                }
                if (currentKey != key) {
                    delta = (int) Math.pow(2, l - counter);
                    i = (currentKey < key) ? (i + delta / 2 + 1) : (i - (delta / 2 + 1));
                }
                else {
                    return positions[i - 1];
                }
            }
        }
        else if (currentKey > key) {
            while (i > 0 && i <= actualLength) {
                counter++;
                currentKey = keys[i - 1];
                comparisonCount.increment();
                if (currentKey == keys[0] && key < currentKey) {
                    break;
                }
                if (delta == 0 && currentKey > key && keys[i - 2] < key) {
                    break;
                }
                if (delta == 0 && currentKey < key && keys[i] > key) {
                    i += 1;
                    break;
                }
                if (currentKey != key) {
                    delta = (int) Math.pow(2, k - counter);
                    i = (currentKey < key) ? (i + delta / 2 + 1) : (i - (delta / 2 + 1));
                }
                else {
                    return positions[i - 1];
                }
            }
        }
        if (isLeaf) {
            return null;
        }
        return children[i - 1].sharSearch(key, comparisonCount);
    }

    public boolean containsKey(int key) {
        for (int i = 0; i < numberOfKeys; i++) {
            if (keys[i] == key) {
                return true;
            }
        }
        return false;
    }

    public void insertNonFull(int key, int position) {
        int i = numberOfKeys - 1;

        if (containsKey(key)) {
            throw new DuplicateKeyException("Key " + key + " already exists in the B-tree.");
        }

        if (isLeaf) {
            while (i >= 0 && key < keys[i]) {
                keys[i + 1] = keys[i];
                positions[i + 1] = positions[i];
                i--;
            }
            keys[i + 1] = key;
            positions[i + 1] = position;
            numberOfKeys++;
        } else {
            while (i >= 0 && key < keys[i]) {
                i--;
            }
            i++;

            if (children[i].numberOfKeys == 2 * t - 1) {
                splitChild(i, children[i]);
                if (key > keys[i]) {
                    i++;
                }
            }
            children[i].insertNonFull(key, position);
        }
    }

    public void splitChild(int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.t, y.isLeaf);
        z.numberOfKeys = t - 1;

        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];
            z.positions[j] = y.positions[j + t];
        }

        if (!y.isLeaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
            }
        }

        y.numberOfKeys = t - 1;

        for (int j = numberOfKeys; j >= i + 1; j--) {
            children[j + 1] = children[j];
        }

        children[i + 1] = z;

        for (int j = numberOfKeys - 1; j >= i; j--) {
            keys[j + 1] = keys[j];
            positions[j + 1] = positions[j];
        }

        keys[i] = y.keys[t - 1];
        positions[i] = y.positions[t - 1];
        numberOfKeys++;
    }

    public void updatePositionsAfterChange(long position, int lengthDifference) {
        for (int i = 0; i < numberOfKeys; i++) {
            if (positions[i] > position) {
                positions[i] += lengthDifference;
            }
        }
        if (!isLeaf) {
            for (int i = 0; i <= numberOfKeys; i++) {
                if (children[i] != null) {
                    children[i].updatePositionsAfterChange(position, lengthDifference);
                }
            }
        }
    }

    private int findActualLength(int[] arr) {
        for (int length = 0; length < arr.length - 1; length++) {
            if (arr[length] != 0 && arr[length + 1] == 0) {
                return length + 1;
            }
        }
        return arr.length;
    }

    public void remove(int key) {
        int index = findKey(key);
        if (index < numberOfKeys && keys[index] == key) {
            if (isLeaf) {
                removeFromLeaf(index);
            }
            else {
                removeFromNonLeaf(index);
            }
        }
        else {
            if (isLeaf) {
                return;
            }
            boolean flag = (index == numberOfKeys);
            if (children[index].numberOfKeys < t) {
                fill(index);
            }
            if (flag && index > numberOfKeys) {
                children[index - 1].remove(key);
            }
            else {
                children[index].remove(key);
            }
        }
    }

    private int findKey(int key) {
        int index = 0;
        while (index < numberOfKeys && keys[index] < key) {
            index++;
        }
        return index;
    }

    private void removeFromLeaf(int index) {
        for (int i = index + 1; i < numberOfKeys; i++) {
            keys[i - 1] = keys[i];
            positions[i - 1] = positions[i];
        }
        numberOfKeys--;
    }

    private void removeFromNonLeaf(int index) {
        int key = keys[index];

        if (children[index].numberOfKeys >= t) {
            int predKey = getPred(index);
            keys[index] = predKey;
            children[index].remove(predKey);
        }
        else if (children[index + 1].numberOfKeys >= t) {
            int succKey = getSucc(index);
            keys[index] = succKey;
            children[index + 1].remove(succKey);
        }
        else {
            merge(index);
            children[index].remove(key);
        }
    }

    private int getPred(int index) {
        BTreeNode curr = children[index];
        while (!curr.isLeaf) {
            curr = curr.children[curr.numberOfKeys];
        }
        return curr.keys[curr.numberOfKeys - 1];
    }

    private int getSucc(int index) {
        BTreeNode curr = children[index + 1];
        while (!curr.isLeaf) {
            curr = curr.children[0];
        }
        return curr.keys[0];
    }

    private void fill(int index) {
        if (index > 0 && children[index - 1].numberOfKeys >= t) {
            borrowFromPrev(index);
        } else if (index < numberOfKeys && children[index + 1].numberOfKeys >= t) {
            borrowFromNext(index);
        } else {
            if (index < numberOfKeys) {
                merge(index);
            } else {
                merge(index - 1);
            }
        }
    }

    private void borrowFromPrev(int index) {
        BTreeNode child = children[index];
        BTreeNode sibling = children[index - 1];

        for (int i = child.numberOfKeys - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
            child.positions[i + 1] = child.positions[i];
        }

        if (!child.isLeaf) {
            for (int i = child.numberOfKeys; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }

        child.keys[0] = keys[index - 1];
        child.positions[0] = positions[index - 1];

        if (!child.isLeaf) {
            child.children[0] = sibling.children[sibling.numberOfKeys];
        }

        keys[index - 1] = sibling.keys[sibling.numberOfKeys - 1];
        positions[index - 1] = sibling.positions[sibling.numberOfKeys - 1];

        child.numberOfKeys++;
        sibling.numberOfKeys--;
    }

    private void borrowFromNext(int index) {
        BTreeNode child = children[index];
        BTreeNode sibling = children[index + 1];

        child.keys[child.numberOfKeys] = keys[index];
        child.positions[child.numberOfKeys] = positions[index];

        if (!child.isLeaf) {
            child.children[child.numberOfKeys + 1] = sibling.children[0];
        }

        keys[index] = sibling.keys[0];
        positions[index] = sibling.positions[0];

        for (int i = 1; i < sibling.numberOfKeys; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
            sibling.positions[i - 1] = sibling.positions[i];
        }

        if (!sibling.isLeaf) {
            for (int i = 1; i <= sibling.numberOfKeys; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        child.numberOfKeys++;
        sibling.numberOfKeys--;
    }

    private void merge(int index) {
        BTreeNode child = children[index];
        BTreeNode sibling = children[index + 1];

        child.keys[t - 1] = keys[index];
        child.positions[t - 1] = positions[index];

        for (int i = 0; i < sibling.numberOfKeys; i++) {
            child.keys[i + t] = sibling.keys[i];
            child.positions[i + t] = sibling.positions[i];
        }

        if (!child.isLeaf) {
            for (int i = 0; i <= sibling.numberOfKeys; i++) {
                child.children[i + t] = sibling.children[i];
            }
        }

        for (int i = index + 1; i < numberOfKeys; i++) {
            keys[i - 1] = keys[i];
            positions[i - 1] = positions[i];
            children[i] = children[i + 1];
        }

        child.numberOfKeys += sibling.numberOfKeys + 1;
        numberOfKeys--;
    }
}
