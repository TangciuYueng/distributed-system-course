package org.cn.edu.tongji.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入命令: ");
            String command = sc.nextLine();

            if (command.startsWith("put")) {
                // 去掉put及其后面的空格
                String filePath = command.replaceFirst("^put\\s+", "");
                // 检查是否在系统中已经存在的文件的路径
                if (isFilePath(filePath)) {
                    long startTime = System.currentTimeMillis();
                    System.out.println("上传的文件为 " + filePath);
                    Upload.UploadFile(filePath);
                    long endTime = System.currentTimeMillis();
                    System.out.println("上传用时: " + (endTime - startTime) + "ms");
                } else {
                    System.out.println("文件不存在，请重新输入~");
                }
            } else if (command.startsWith("get")) {
                // 去掉get及其后面的空格
                String fileName = command.replaceFirst("^get\\s+", "");
                long startTime = System.currentTimeMillis();
                Download.downloadFile(fileName);
                long endTime = System.currentTimeMillis();
                System.out.println("下载用时: " + (endTime - startTime) + "ms");
            } else if (command.startsWith("cput")) {
                // 去掉cput及其后面的空格
                String filePath = command.replaceFirst("^cput\\s+", "");
                // 检查是否在系统中已经存在的文件的路径
                if (isFilePath(filePath)) {
                    long startTime = System.currentTimeMillis();
                    System.out.println("上传的文件为 " + filePath);
                    CUpload.CUploadFile(filePath);
                    long endTime = System.currentTimeMillis();
                    System.out.println("上传用时: " + (endTime - startTime) + "ms");
                } else {
                    System.out.println("文件不存在，请重新输入~");
                }
            } else if (command.startsWith("cget")) {
                // 去掉cget及其后面的空格
                String fileName = command.replaceFirst("^cget\\s+", "");
                long startTime = System.currentTimeMillis();
                CDownload.CDownloadFile(fileName);
                long endTime = System.currentTimeMillis();
                System.out.println("下载用时: " + (endTime - startTime) + "ms");
            } else if (command.equals("check")) {
                System.out.println("CGET!");
            } else if (command.equals("q")) {
                break;
            } else {
                System.out.println("没有这个命令~请输入: put/get/cput/cget/check/q");
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
