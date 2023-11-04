import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.StandardOpenOption;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Upload {
    private String filePath;
    private String fileName;
    private String fileExt;
    private int chunkCount;
    private static final String BASE_CHUNK_FILE_PATH = "./";
    // 1024KB
    // private static final int CHUNK_SIZE = 1024 * 1024;
    private static final int CHUNK_SIZE = 32;
    // 4 * 1024B
    private static final int BUFFER_SIZE = 4 * 1024;
    // 连接服务端
    private static String SERVER_HOST = "localhost";
    private static int SERVER_PORT = 8888;

    public Upload(String filePath) {
        this.filePath = filePath;
        Path path = Paths.get(filePath);
        this.fileName = path.getFileName().toString();
        this.fileExt = "";
        int dotIndex = this.fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            this.fileExt = this.fileName.substring(dotIndex + 1);
            this.fileName = this.fileName.substring(0, dotIndex);
        }
        // System.out.println(this.fileName + " " + this.filePath + " " + this.fileExt);
    }
    // 将文件分割成为多个块
    public void getChunk() {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
                FileChannel fileChannel = file.getChannel()) {
            // 总共需要处理的字节数
            long fileSize = file.length();
            // 已经完成的字节数量
            long btyesRead = 0;
            // 产生的块数
            chunkCount = (int) Math.ceil((double) fileSize / CHUNK_SIZE);

            // 一个个读取块
            for (int i = 0; i < chunkCount; ++i) {
                String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + i + "." + fileExt;
                try (RandomAccessFile chunkFile = new RandomAccessFile(chunkFilePath, "rw");
                        FileChannel chunkFileChannel = chunkFile.getChannel()) {
                    // 到最后超出了自动判断实际需要复制的字节
                    fileChannel.transferTo(i * CHUNK_SIZE, CHUNK_SIZE, chunkFileChannel);
                } catch(Exception e) {
                    System.out.println("创建" + chunkFilePath + "出错");
                }
            }
        } catch (Exception e) {
            System.out.println("读取" + filePath + "出错");
        }
    }
    // 上传服务端
    public void sendChunk() {
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(SERVER_HOST, SERVER_PORT))) {
            // 设置非阻塞模式
            socketChannel.configureBlocking(false);
            
            for (int i = 0; i < chunkCount; ++i) {

                // 文件名转换成为字节数组
                String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + i + "." + fileExt;
                byte[] fileNameBytes = chunkFilePath.getBytes();
                ByteBuffer fileNameBuffer = ByteBuffer.allocate(fileNameBytes.length);
                // 发送文件名长度
                ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                fileNameLengthBuffer.putInt(fileNameBytes.length);
                fileNameLengthBuffer.flip();
                socketChannel.write(fileNameLengthBuffer);
                fileNameLengthBuffer.clear();
                // 发送文件名
                fileNameBuffer.put(fileNameBytes);
                fileNameBuffer.flip();
                socketChannel.write(fileNameBuffer);
                fileNameBuffer.clear();

                // 发送文件内容
                try (FileChannel fileChannel = FileChannel.open(Paths.get(chunkFilePath), StandardOpenOption.READ)) {
                    // 发送文件内容长度
                    ByteBuffer fileLengthBuffer = ByteBuffer.allocate(Long.BYTES);
                    fileLengthBuffer.putLong(fileChannel.size());
                    fileLengthBuffer.flip();
                    socketChannel.write(fileLengthBuffer);
                    fileLengthBuffer.clear();
                    // 发送文件内容
                    fileChannel.transferTo(0, fileChannel.size(), socketChannel);
                    System.out.println("发送文件" + chunkFilePath + "成功");
                } catch (IOException e) {
                    System.out.println("发送文件" + chunkFilePath + "出问题： " + e.getMessage());
                    // 可以根据需求进行错误处理，例如记录日志或终止发送过程
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Upload upload = new Upload("text.txt");
        upload.getChunk();
        upload.sendChunk();
    } 
}