# 网络编程
## POSIX
POSIX（Portable Operating System Interface for Unix）是一种操作系统标准接口，旨在提供可移植性和兼容性，使不同的Unix系统能够在不同平台上运行相同的代码。它定义了一系列API（应用程序接口）和命令行工具，包括文件操作、进程管理、信号处理、线程支持等等。
[POSIX](https://zhuanlan.zhihu.com/p/392588996)

## TCP三次握手四次挥手
通过字节流
建立连接通过三次报文握手
释放连接通过四次挥手
[csdn](https://blog.csdn.net/m0_56649557/article/details/119492899)

## 连接池
网络连接池提升性能，每次都新建连接会消耗大量系统资源和时间，连接池可以提前建立一定数量的连接，并将这些连接保存在池中，应用程序可以随时从连接池中获取一个可用的连接来执行数据库操作，执行完毕后再将连接返回给连接池

## 热数据
热数据经常访问缓存在内存中，减少I/O
[zhihu](https://zhuanlan.zhihu.com/p/210486889#:~:text=%E6%AF%94%E5%A6%82%E5%9C%A8%E8%AE%A2%E5%8D%95%E7%AE%A1%E7%90%86%E4%B8%AD,%E5%87%A0%E4%B9%8E%E4%B8%8D%E4%BC%9A%E8%A2%AB%E7%94%A8%E5%88%B0%E3%80%82)

而工业上
- 热数据是需要被**计算节点频繁访问**的在线类数据，比如可以是半年以内的数据，用户经常会查询它们，适合放在数据库中存储，比如MySql、MongoDB和HBase。
- 温数据是**非即时的状态和行为数据**，也可以简单理解为把热数据和冷数据混在一起就成了温数据。如果整体数据量不大，也可以不区分温数据和热数据。
- 冷数据是指离线类不经常访问的数据，用于灾难恢复的备份或者因为要遵守法律规定必须保留一段时间，比如企业备份数据、业务与操作日志数据、话单与统计数据。通常会存储在性能较低、价格较便宜的文件系统里，适用于离线分析，比如机器学习中的模型训练或者大数据分析。

冷热分离
对于热数据系统，需要重点考虑读写的性能问题，诸如MySQL、Elasticsearch等会成为首选；而对于冷数据系统，则需要重点关注低成本存储问题，通常会选择存储在HDFS或云对象存储中，再选择一个相应的查询系统。

## Java 实现
当涉及到TCP和UDP的解决方法时，主要是指在Java中使用TCP和UDP协议进行网络通信时的编程实现。

1. TCP解决方法：
在Java中，使用TCP协议进行网络通信主要通过`Socket`和`ServerSocket`类进行编程实现。以下是TCP的解决方法的示例代码：

客户端代码（TCPClient.java）：
```java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        try (Socket socket = new Socket(host, port)) {
            // 发送数据
            OutputStream outputStream = socket.getOutputStream();
            String message = "Hello, server!";
            outputStream.write(message.getBytes());

            // 接收响应
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String response = new String(buffer, 0, bytesRead);
            System.out.println("收到服务器响应：" + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

服务器端代码（TCPServer.java）：
```java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static void main(String[] args) {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("等待客户端连接...");

            Socket socket = serverSocket.accept();
            System.out.println("客户端已连接：" + socket.getInetAddress().getHostAddress());

            // 接收数据
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String message = new String(buffer, 0, bytesRead);
            System.out.println("收到客户端消息：" + message);

            // 发送响应
            OutputStream outputStream = socket.getOutputStream();
            String response = "Hello, client!";
            outputStream.write(response.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

以上代码示例中，客户端通过创建一个`Socket`对象与服务器建立连接，然后通过`OutputStream`发送数据，并通过`InputStream`接收服务器的响应。服务器端通过创建一个`ServerSocket`对象监听指定端口，接收来自客户端的连接，然后通过`InputStream`接收客户端的数据，并通过`OutputStream`发送响应。

还可以这样
服务端
```java
package com.itest.socket2;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPEchoServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12900, 100, InetAddress.getByName("localhost"));
        System.out.println("Server started at: " + serverSocket);

        while (true) {
            System.out.printf("Waiting for a connection...");

            final Socket activeSocket = serverSocket.accept();

            System.out.println("Received a connection from " + activeSocket);

            Runnable runnable = () -> handleClientRequest(activeSocket);
            new Thread(runnable).start();
        }
    }

    private static void handleClientRequest(Socket activeSocket) {
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(activeSocket.getOutputStream()));

            String inMsg = null;

            while((inMsg = bufferedReader.readLine()) != null) {
                System.out.println("Received from client: " + inMsg);

                String outMsg = inMsg;
                bufferedWriter.write("I received: " + outMsg + "\n");
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                activeSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

```
客户端
```java
package com.itest.socket2;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPEchoClient {
    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            socket = new Socket("localhost", 12900);
            System.out.println("Started client socket at " + socket.getLocalSocketAddress());

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            BufferedReader consoleReader = new BufferedReader((new InputStreamReader(System.in)));
            String promptMsg = "Please enter a message (Bye to quit):";
            String outMsg = null;

            while ((outMsg = consoleReader.readLine()) != null) {
                if (outMsg.equalsIgnoreCase("bye")) {
                    break;
                }
                bufferedWriter.write(outMsg + "\n");
                bufferedWriter.flush();

                String inMsg = bufferedReader.readLine();
                System.out.printf("Server: " + inMsg);

                System.out.println();
                System.out.printf(promptMsg);
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

```

2. UDP解决方法：
在Java中，使用UDP协议进行网络通信主要通过`DatagramSocket`和`DatagramPacket`类进行编程实现。以下是UDP的解决方法的示例代码：

客户端代码（UDPClient.java）：
```java
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        try (DatagramSocket socket = new DatagramSocket()) {
            // 发送数据
            String message = "Hello, server!";
            byte[] sendData = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName(host);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            socket.send(sendPacket);

            // 接收响应
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("收到服务器响应：" + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

服务器端代码（UDPServer.java）：
```java
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {
    public static void main(String[] args) {
        int port = 8080;

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("等待客户端连接...");

            // 接收数据
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("收到客户端消息：" + message);

            // 发送响应
            byte[] sendData = "Hello, client!".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
            socket.send(sendPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

以上代码示例中，客户端通过创建一个`DatagramSocket`对象发送数据报文，并通过`DatagramPacket`指定目标地址和端口，然后通过`DatagramSocket`的`send()`方法发送数据。服务器端通过创建一个`DatagramSocket`对象监听指定端口，接收客户端发送的数据报文，并通过`DatagramPacket`获取数据和发送响应。

区别：
- TCP是面向连接的协议，UDP是无连接的协议。TCP提供可靠的、面向连接的通信，确保数据传输的准确性和顺序，而UDP则不提供可靠性和顺序保证。
- TCP使用的是字节流的传输方式，UDP使用的是数据报文的方式。
- TCP需要在客户端和服务器之间建立连接，而UDP不需要建立连接。
- TCP是全双工的，即客户端和服务器可以同时发送和接收数据，而UDP是单向的，即一端发送数据，另一端接收数据。
- TCP在数据传输时有较高的开销，因为它需要进行连接的建立和维护，而UDP则没有这些开销，因而更加轻量级。
- TCP的可靠性和顺序保证使得它适用于对数据传输可靠性要求较高的应用场景，如文件传输、网页浏览等。而UDP适用于实时性要求较高、对可靠性要求较低的应用场景，如实时视频、音频传输等。

## URL类
发送一百万个请求，理论上它可以宕机

### 常用属性
URL 类是 Java 标准库中的一个类，用于表示 Uniform Resource Locator（统一资源定位符）。它可以用来处理与互联网资源的通信，例如打开连接、读取和写入数据等操作。

下面是 URL 类的一些常用属性：

1. **Scheme（协议）**：URL 的 scheme 指定了访问资源所使用的协议，例如 HTTP、HTTPS、FTP 等。它通常出现在 URL 的开头，后面跟着冒号（:）。例如，"http://"、"https://"。

2. **Hostname（主机名）**：URL 的 hostname 指定了要访问的主机的名称或 IP 地址。它紧跟在 scheme 后面，并以双斜线（//）分隔。例如，"www.example.com"。

3. **Port（端口）**：URL 的 port 是可选的，它指定了要连接的主机上的端口号。如果未指定端口号，则使用默认端口号。通常，不同的协议使用不同的默认端口号，例如 HTTP 默认端口为 80，HTTPS 默认端口为 443。端口号位于主机名后面，使用冒号（:）分隔。例如，"http://www.example.com:8080"。

4. **Path（路径）**：URL 的 path 指定了要访问的资源的路径。路径是以斜杠（/）开始的字符串，可以包含多个路径段。例如，"/path/to/resource"。

5. **Query String（查询字符串）**：URL 的 query string 是可选的，它包含了向服务器传递的参数。它通常出现在路径之后，并以问号（?）开头。参数以键值对的形式出现，使用等号（=）将键和值分隔，多个参数之间使用和号（&）分隔。例如，"/path?param1=value1&param2=value2"。

下面是使用 URL 类的示例代码：

```java
import java.net.URL;

public class URLExample {
    public static void main(String[] args) {
        try {
            // 创建 URL 对象
            URL url = new URL("https://www.example.com/path?param=value");

            // 获取 URL 的属性
            String scheme = url.getProtocol();
            String hostname = url.getHost();
            int port = url.getPort();
            String path = url.getPath();
            String query = url.getQuery();

            // 打印属性值
            System.out.println("Scheme: " + scheme);
            System.out.println("Hostname: " + hostname);
            System.out.println("Port: " + port);
            System.out.println("Path: " + path);
            System.out.println("Query String: " + query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 构造函数
URL 类提供了几种构造函数，用于创建 URL 对象。下面是 URL 类的几种常用构造函数：

1. **URL(String spec)**：使用指定的字符串创建 URL 对象。该字符串表示完整的 URL，包括 scheme、hostname、port、path 和 query string。例如，`URL url = new URL("https://www.example.com/path?param=value");`

2. **URL(String protocol, String host, int port, String file)**：以指定的协议、主机名、端口和路径创建 URL 对象。例如，`URL url = new URL("https", "www.example.com", 8080, "/path");`

3. **URL(String protocol, String host, String file)**：以指定的协议、主机名和路径创建 URL 对象。默认使用该协议的默认端口。例如，`URL url = new URL("https", "www.example.com", "/path");`

4. **URL(URL base, String spec)**：使用基础 URL 和相对路径字符串创建 URL 对象。基础 URL 是一个已存在的 URL，而相对路径字符串是相对于基础 URL 的路径。例如，`URL baseURL = new URL("https://www.example.com"); URL url = new URL(baseURL, "/path");`

### 获取信息
1. **Retrieving URL Contents（检索 URL 内容）**：
   - 使用 `openStream()` 方法可以获取 URL 的字节输入流，从而读取 URL 的内容。例如：`InputStream inputStream = url.openStream();`
   - 可以将字节输入流转换为字符输入流，并使用 BufferedReader 等来读取 URL 内容。例如：`BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));`
   - `String getContentType()`
   - `String getContentLength()`
   - `long getLastModified()`

2. **Getting Header Information（获取头信息）**：
   - 使用 `openConnection()` 方法打开与 URL 的连接，并返回一个 URLConnection 对象。例如：`URLConnection connection = url.openConnection();`
   - 通过 URLConnection 对象，可以使用 `getHeaderField()` 方法获取特定头字段的值。例如：`String contentType = connection.getHeaderField("Content-Type");`

3. **URLConnection（连接 URL）**：
   - URLConnection 类是一个抽象类，表示与 URL 的连接。您可以使用 `openConnection()` 方法获取 URLConnection 对象并建立连接。
   - 通过 URLConnection，您可以获取与 URL 相关的各种信息，例如响应代码、响应消息、内容长度等。
   - 您还可以通过设置请求属性、读取响应数据和处理响应流等方法来与服务端进行交互。

