package cn.edu.tongji.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("enter the command");
            String command = scanner.nextLine();

            if (command.startsWith("put")) {
                // get rid of the put and backspace after it
                String filePath = command.replaceFirst("^put\\s+", "");
                if (isFilePath(filePath)) {
                    System.out.println(filePath + "will be uploaded");
                    long startTime = System.currentTimeMillis();
                    Upload.UploadFile(filePath);
                    long endTime = System.currentTimeMillis();
                    System.out.println("duration: " + (endTime - startTime) + "ms");
                } else {
                    System.out.println("file doesn't exist");
                }
            } else if (command.startsWith("get")) {
                // get rid of the get and backspace after it
                String fileName = command.replaceFirst("^get\\s+", "");
                long startTime = System.currentTimeMillis();
                Download.downloadFile(fileName);
                long endTime = System.currentTimeMillis();
                System.out.println("duration: " + (endTime - startTime) + "ms");
            } else if (command.startsWith("cput")) {
                // get rid of the cput and backspace after it
                String filePath = command.replaceFirst("^cput\\s+", "");
                if (isFilePath(filePath)) {
                    long startTime = System.currentTimeMillis();
                    CUpload.UploadFile(filePath);
                    long endTime = System.currentTimeMillis();
                    System.out.println("duration: " + (endTime - startTime) + "ms");
                } else {
                    System.out.println("file doesn't exist");
                }
            } else if (command.startsWith("cget")) {
                // get rid of the cget and backspace after it
                String fileName = command.replaceFirst("^cget\\s+", "");
                long startTime = System.currentTimeMillis();
                CDownload.downloadFile(fileName);
                long endTime = System.currentTimeMillis();
                System.out.println("duration: " + (endTime - startTime) + "ms");
            } else if (command.startsWith("check")) {
                // get rid of the check and backspace after it
                String fileName = command.replaceFirst("^check\\s+", "");
                CheckStatus checkStatus = new CheckStatus(fileName);
                checkStatus.check();
            } else if (command.startsWith("q")) {
                break;
            } else {
                System.out.println("command error: put/get/cput/cget/check/q");
            }
        }
    }

    private static boolean isFilePath(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.isRegularFile(path);
        } catch (Exception e) {
            return false;
        }
    }
}
