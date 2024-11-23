package com.algorithms.save;

import com.algorithms.BTree;

import java.io.*;

public class BTreeUtils {
    public static void saveBTree(BTree btree, String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(btree);
        }
    }
    public static BTree loadBTree(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (BTree) in.readObject();
        }
    }
}
