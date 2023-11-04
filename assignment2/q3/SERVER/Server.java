import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;


public class Server {
    private static final int BUFFER_SIZE = 4 * 1024;
    public static void main(String[] args) {
        int SERVER_PORT = 8888;
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_PORT));
            System.out.println("服务器已启动，等待客户端连接...");

            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("客户端已连接：" + socketChannel.getRemoteAddress());
                handleClient(socketChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(SocketChannel socketChannel) {
        try {
            while (true) {

            
                // 接收文件名长度
                ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                if (socketChannel.read(fileNameLengthBuffer) < 0) {
                    break;
                }
                fileNameLengthBuffer.flip();
                int fileNameLength = fileNameLengthBuffer.getInt();
                System.out.println("文件名长度为: " + fileNameLength);

                // 接收文件名
                ByteBuffer fileNameBuffer = ByteBuffer.allocate(fileNameLength);
                int bytesRead = socketChannel.read(fileNameBuffer);
                fileNameBuffer.flip();
                byte[] fileNameBytes = new byte[bytesRead];
                fileNameBuffer.get(fileNameBytes);
                String fileName = new String(fileNameBytes);
                fileName = fileName.trim();
                System.out.println("接收到文件名：" + fileName);

                // 接收文件长度
                ByteBuffer fileLengthBuffer = ByteBuffer.allocate(Long.BYTES);
                socketChannel.read(fileLengthBuffer);
                fileLengthBuffer.flip();
                long fileLength = fileLengthBuffer.getLong();
                System.out.println("文件长度为: " + fileLength);

                // 接收文件内容
                try (FileChannel fileChannel = FileChannel.open(Paths.get(fileName), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

                    long fileBytesRead = 0;
                    ByteBuffer fileBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    while (fileBytesRead < fileLength) {
                        long remaining = fileLength - fileBytesRead;
                        int toReadSize = (int) Math.min(remaining, BUFFER_SIZE);
                        fileBuffer.limit(toReadSize);
                        int fileBytesReadNow = socketChannel.read(fileBuffer);
                        if (fileBytesReadNow == -1) {
                            break;
                        }
                        fileBuffer.flip();
                        fileChannel.write(fileBuffer);
                        fileBytesRead += fileBytesReadNow;
                        fileBuffer.clear();
                    }
                    System.out.println("文件接收完成" + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}