package com.algorithms.data;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileAccess {
    public static String readRecord(String filePath, int position) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(position);
            return file.readLine();
        }
    }
    public static long writeRecord(String filePath, String record) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek(file.length());
             long position = file.getFilePointer();
             file.writeBytes(record + "\n");
             return position;
        }
    }
}
