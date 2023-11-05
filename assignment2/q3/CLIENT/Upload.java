import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            // 产生的块数
            chunkCount = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
            // 循环读取块的过程放入一个线程池中
            ExecutorService executorService = Executors.newFixedThreadPool(chunkCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < chunkCount; ++i) {
                int chunkIndex = i;
                // 封装为异步任务交给线程池处理
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + chunkIndex + "." + fileExt;
                    try (RandomAccessFile chunkFile = new RandomAccessFile(chunkFilePath, "rw");
                        FileChannel chunkFileChannel = chunkFile.getChannel()) {
                        long position = chunkIndex * CHUNK_SIZE;
                        long remaining = fileSize - position;
                        long chunkSize = Math.min(CHUNK_SIZE, remaining);
                        fileChannel.transferTo(position, chunkSize, chunkFileChannel);
                    } catch (Exception e) {
                        System.out.println("创建" + chunkFilePath + "出错");
                    }
                }, executorService);
                futures.add(future);
            }
            // 等待所有任务完成关闭线程池
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executorService.shutdown();

        } catch (Exception e) {
            System.out.println("读取" + filePath + "出错");
            e.printStackTrace();
        }
    }
    // 上传服务端
    public void sendChunk() {
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(SERVER_HOST, SERVER_PORT))) {
            // 设置非阻塞模式
            socketChannel.configureBlocking(false);
            
            for (int i = 0; i < chunkCount; ++i) {
                String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + i + "." + fileExt;
                // 打开chunk文件
                try (FileChannel fileChannel = FileChannel.open(Paths.get(chunkFilePath), StandardOpenOption.READ)) {
                    // 文件名转换成为字节数组
                    byte[] fileNameBytes = chunkFilePath.getBytes(StandardCharsets.UTF_8);
                    // 发送文件名长度
                    ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                    fileNameLengthBuffer.putInt(fileNameBytes.length);
                    fileNameLengthBuffer.flip();
                    socketChannel.write(fileNameLengthBuffer);
                    // 发送文件名
                    ByteBuffer fileNameBuffer = ByteBuffer.wrap(fileNameBytes);
                    socketChannel.write(fileNameBuffer);
                    fileNameBuffer.clear();

                    // 发送文件内容长度
                    ByteBuffer fileLengthBuffer = ByteBuffer.allocate(Long.BYTES);
                    fileLengthBuffer.putLong(fileChannel.size());
                    fileLengthBuffer.flip();
                    socketChannel.write(fileLengthBuffer);
                    // 发送文件内容
                    fileChannel.transferTo(0, fileChannel.size(), socketChannel);

                    System.out.println("发送文件" + chunkFilePath + "成功");
                } catch (IOException e) {
                    System.out.println("发送文件" + chunkFilePath + "出问题： " + e.getMessage());
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