package com.algorithms.data;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileAccess {
    public static String readRecord(String filePath, int position) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(position); // Переміщаємося до потрібної позиції
            return file.readLine(); // Читаємо рядок
        }
    }
    public static long writeRecord(String filePath, String record) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek(file.length());
             long position = file.getFilePointer(); // Отримуємо позицію
             file.writeBytes(record + "\n");
             return position;
        }
    }
}
