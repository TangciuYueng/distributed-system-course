package org.cn.edu.tongji.client;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入命令: ");
        String command = sc.next().toString();

        if (command.equals("put")) {
            long startTime = System.currentTimeMillis();
            System.out.println("PUT!");
            long endTime = System.currentTimeMillis();
            System.out.println("上传用时: " + (endTime - startTime) + "ms");
        } else if (command.equals("get")) {
            long startTime = System.currentTimeMillis();
            System.out.println("GET!");
            long endTime = System.currentTimeMillis();
            System.out.println("上传用时: " + (endTime - startTime) + "ms");
        } else if (command.equals("cput")) {
            long startTime = System.currentTimeMillis();
            System.out.println("CPUT!");
            long endTime = System.currentTimeMillis();
            System.out.println("上传用时: " + (endTime - startTime) + "ms");
        } else if (command.equals("cget")) {
            long startTime = System.currentTimeMillis();
            System.out.println("CGET!");
            long endTime = System.currentTimeMillis();
            System.out.println("上传用时: " + (endTime - startTime) + "ms");

        } else if (command.equals("check")) {
            System.out.println("CGET!");

        } else {
            System.out.println("没有这个命令~请输入: put/get/cput/cget/check");
        }
    }
}
