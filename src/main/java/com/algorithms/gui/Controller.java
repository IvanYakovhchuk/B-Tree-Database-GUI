package com.algorithms.gui;

import com.algorithms.BTree;
import com.algorithms.BTreeNode;
import com.algorithms.exceptions.DuplicateKeyException;
import com.algorithms.save.BTreeUtils;
import com.algorithms.data.FileAccess;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Random;

public class Controller {
    private final static String dataFile = "data.txt";
    @FXML
    private MenuItem deleteAllDataItem;
    @FXML
    private MenuItem randomRecordsItem;
    @FXML
    private MenuItem allRecordsItem;
    @FXML
    private MenuItem clearInputItem;
    @FXML
    private TextField keyInput;
    @FXML
    private TextArea recordInput;
    @FXML
    private Button addButton;
    @FXML
    private Button findButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label messagesOutput;

    private BTree bTree;

    public void initialize() throws IOException, ClassNotFoundException {
        bTree = BTreeUtils.loadBTree("btree.ser");
        if (bTree == null) {
            bTree = new BTree(25);
        }
    }
    public void setCloseHandler(Stage stage) {
        stage.setOnCloseRequest(event -> {
            try {
                BTreeUtils.saveBTree(bTree, "btree.ser");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Platform.exit();
        });
    }

    @FXML
    private void addRecord() throws IOException {
        String keyText = "";
        try {
            keyText = keyInput.getText();
            int key = Integer.parseInt(keyText);
            String record = recordInput.getText();
            bTree.insert(key, (int)FileAccess.writeRecord(dataFile, record));
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("Record (" + record + ") added successfully at key " + keyText + "!");
            messagesOutput.setTextFill(Paint.valueOf("GREEN"));
        }
        catch (NumberFormatException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("Please, enter a valid key!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
        catch (DuplicateKeyException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("Record with key " + keyText + " already exists!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
    }
    @FXML
    private void findRecord() throws IOException {
        try {
            String keyText = keyInput.getText();
            int key = Integer.parseInt(keyText);
            long positionToRead = bTree.search(key);
            String res = FileAccess.readRecord(dataFile, (int)positionToRead);
            recordInput.setText(res);
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("Record found: " + res);
            messagesOutput.setTextFill(Paint.valueOf("GREEN"));
        }
        catch (NullPointerException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("No record with such key found!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
        catch (NumberFormatException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("Please, enter a valid key!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
    }

    @FXML
    private void deleteAllData() throws IOException {
        try {
            Files.delete(Path.of("data.txt"));
        }
        catch (NoSuchFileException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("No file found!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
        Files.deleteIfExists(Path.of("btree.ser"));
        this.bTree = new BTree(25);
        BTreeUtils.saveBTree(bTree, "btree.ser");
        messagesOutput.setVisible(true);
        messagesOutput.setWrapText(true);
        messagesOutput.setFont(new Font("System", 20));
        messagesOutput.setText("All data deleted successfully!");
        messagesOutput.setTextFill(Paint.valueOf("GREEN"));
    }

    @FXML
    private void generateRecords() throws IOException {
        Random rand = new Random();
        for (int i = 0; i < 10000; i++) {
            int key = i + 1;
            int stringLength = rand.nextInt(51);
            String generatedString = generateRandomString(stringLength);
            try {
            bTree.insert(key, (int)FileAccess.writeRecord(dataFile, generatedString));
            }
            catch (DuplicateKeyException e) {
                e.printStackTrace();
            }
        }
        messagesOutput.setVisible(true);
        messagesOutput.setWrapText(true);
        messagesOutput.setFont(new Font("System", 20));
        messagesOutput.setText("Data generated successfully!");
        messagesOutput.setTextFill(Paint.valueOf("GREEN"));
    }
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    @FXML
    private void editRecord() throws IOException {
        String keyText = "";
        try {
            String newRecord = recordInput.getText();
            keyText = keyInput.getText();
            int key = Integer.parseInt(keyText);
            long positionToWrite = bTree.search(key);
            try (RandomAccessFile file = new RandomAccessFile(dataFile, "rw")) {
                file.seek(positionToWrite);
                String oldRecord = file.readLine();
                int lengthDifference = newRecord.length() - oldRecord.length();
                byte[] buffer = new byte[1024];
                long shiftStart = positionToWrite + oldRecord.length() + 1;
                long shiftEnd = file.length();
                file.seek(shiftEnd);

                while (shiftEnd > shiftStart) {
                    long readPosition = Math.max(shiftStart, shiftEnd - buffer.length);
                    file.seek(readPosition);
                    int bytesRead = file.read(buffer);
                    file.seek(readPosition + lengthDifference);
                    file.write(buffer, 0, bytesRead);
                    shiftEnd -= bytesRead;
                }
                file.seek(positionToWrite);
                file.writeBytes(newRecord);
                bTree.updatePositionsInBTree(positionToWrite, lengthDifference);

                file.writeBytes("\n");
                recordInput.setText(newRecord);
                messagesOutput.setVisible(true);
                messagesOutput.setWrapText(true);
                messagesOutput.setFont(new Font("System", 12));
                messagesOutput.setText("Record (" + oldRecord + ") has been edited successfully! Now this record is (" + newRecord + ")");
                messagesOutput.setTextFill(Paint.valueOf("GREEN"));
            }
        }
        catch (NullPointerException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("No record with key " + keyText + " found!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
        catch (NumberFormatException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("Please, enter a valid key!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
    }

    @FXML
    private void deleteRecord() throws IOException {
        String keyText = "";
        try {
            keyText = keyInput.getText();
            int key = Integer.parseInt(keyText);
            long positionToWrite = bTree.search(key);

            try (RandomAccessFile file = new RandomAccessFile(dataFile, "rw")) {
                file.seek(positionToWrite);
                String oldRecord = file.readLine();
                file.seek(positionToWrite);
                file.writeBytes("");
                int lengthOfDeletedRecord = oldRecord.length() + 1;

                long shiftStart = positionToWrite + lengthOfDeletedRecord;
                long shiftEnd = file.length();
                file.seek(shiftEnd);

                byte[] buffer = new byte[1024];
                while (shiftEnd > shiftStart) {
                    long readPosition = Math.max(shiftStart, shiftEnd - buffer.length);
                    file.seek(readPosition);
                    int bytesRead = file.read(buffer);
                    file.seek(readPosition - lengthOfDeletedRecord);
                    file.write(buffer, 0, bytesRead);
                    shiftEnd -= bytesRead;
                }

                file.setLength(file.length() - lengthOfDeletedRecord);
                bTree.remove(key);
                bTree.updatePositionsInBTree(positionToWrite, -lengthOfDeletedRecord);

                messagesOutput.setVisible(true);
                messagesOutput.setWrapText(true);
                messagesOutput.setFont(new Font("System", 20));
                messagesOutput.setText("Record with key " + key + " has been deleted successfully.");
                messagesOutput.setTextFill(Paint.valueOf("GREEN"));
            }
        }
        catch (NullPointerException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("No record with key " + keyText + " found!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
        catch (NumberFormatException e) {
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("Please, enter a valid key!");
            messagesOutput.setTextFill(Paint.valueOf("RED"));
        }
    }

    @FXML
    private void showAllRecords() throws IOException {
        if (bTree != null) {
            StringBuilder sb = new StringBuilder();
            try (RandomAccessFile file = new RandomAccessFile(dataFile, "rw")){
                showInOrder(bTree.root, sb, file);
            }
            recordInput.setText(sb.toString());
            messagesOutput.setVisible(true);
            messagesOutput.setWrapText(true);
            messagesOutput.setFont(new Font("System", 20));
            messagesOutput.setText("All data is now on the screen!");
            messagesOutput.setTextFill(Paint.valueOf("GREEN"));
        }
    }

    private void showInOrder(BTreeNode node, StringBuilder sb, RandomAccessFile file) throws IOException {
        if (node != null) {
            for (int i = 0; i < node.numberOfKeys; i++) {
                if (!node.isLeaf) {
                    showInOrder(node.children[i], sb, file);
                }

                sb.append("Key ").append(node.keys[i] + ": ").append(FileAccess.readRecord(dataFile, node.positions[i])).append("\n");
            }

            if (!node.isLeaf) {
                showInOrder(node.children[node.numberOfKeys], sb, file);
            }
        }
    }

    @FXML
    private void clearInput() {
        keyInput.setText("");
        recordInput.setText("");
    }
}