package cn.edu.tongji.component;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupServer {
    public static MembershipManager membershipManager;

    public static void main(String[] args) throws IOException {
        try (ExecutorService executorService = Executors.newCachedThreadPool()) {

            String address = args[0];
            membershipManager = new MembershipManager(address);

            // 多线程启动 Query 服务
            executorService.execute(new Thread(new QueryService()));
        }

        System.out.println("show - show the member list");
        System.out.println("exit - leave the member list");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("plz input the commander") ;
            String input = scanner.nextLine();

            if (input.equals("show")) {
                if (membershipManager.isJoin) {
                    membershipManager.showMemberList();
                } else {
                    System.out.println("did not join");
                    membershipManager.joinIn();
                }
            } else if (input.equals("exit")) {
                if (membershipManager.isJoin) {
                    membershipManager.leave();
                } else {
                    System.out.println("has exited");
                }
            }
        }
    }

}
