package cn.edu.tongji.component;

import lombok.Data;

import java.io.*;
import java.util.Scanner;

@Data
public class Logger {
    private RandomAccessFile randomAccessFile;

    public Logger(String fileName) {
        File file = new File(fileName + ".log");
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            if (file.createNewFile()) {
                System.out.println("[query]: Created new log file: " + file.getName());
            } else {
                System.out.println("[query]: Log file " + file.getName() + " already exists.");
            }
        } catch (IOException e) {
            System.out.println("[queryError]: Error creating/accessing log file.");
            e.printStackTrace();
        }
    }

    public void writeInfo(String text) {
        // Write text to the end of the log file
        try {
            long timestamp = System.currentTimeMillis();
            randomAccessFile.seek(randomAccessFile.length());
            randomAccessFile.writeBytes(timestamp + " " + text + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("[queryError]: Error writing to log file.");
            e.printStackTrace();
        }
    }

    public String query(String keyword) {
        StringBuilder result = new StringBuilder();
        try {
            randomAccessFile.seek(0);
            System.out.println("[query]: Start querying file");
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                if (new String(line.getBytes("ISO-8859-1"), "UTF-8").contains(keyword)) {
                    result.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            System.out.println("[queryError]: Error reading log file.");
            e.printStackTrace();
        }
        return result.toString();
    }

    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            System.out.println("[queryError]: Error closing log file.");
            e.printStackTrace();
        }
    }
}
