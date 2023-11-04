import java.net.URL;

public class URLComparator {
    public static boolean sameURL(String url1, String url2) {
        try {
            URL parsedURL1 = new URL(url1);
            URL parsedURL2 = new URL(url2);

            String host1 = parsedURL1.getHost(); // 提取域名部分
            String[] host1Parts = host1.split("\\.");

            String host2 = parsedURL2.getHost();
            String[] host2Parts = host2.split("\\.");

            if (host1Parts.length < 3 || host2Parts.length < 3) {
                return false;
            }
            System.out.println(host1);
            System.out.println(host2);

            return host1Parts[host1Parts.length - 1].equals(host2Parts[host2Parts.length - 1])
                    && host1Parts[host1Parts.length - 2].equals(host2Parts[host2Parts.length - 2])
                    && host1Parts[host1Parts.length - 3].equals(host2Parts[host2Parts.length - 3]);
        } catch (Exception e) {
            // 处理 URL 解析错误的异常情况
            e.printStackTrace();
            return false;
        }
    }
    
    public static void main(String[] args) {
        String url1 = "https://mp.weixin.qq.com/s/EVQiZRGsPCD-1HWCJNq29g";
        String url2 = "https://mp.weixin.qq.com/s/EnpSQ9KJflaNsHacT_C4Rw";
        System.out.println(sameURL(url1, url2));
    }
}