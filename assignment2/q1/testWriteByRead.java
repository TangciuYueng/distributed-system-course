import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;

public class FileReadingExample {
    public static void main(String[] args) {
        try {
            // 创建FileInputStream和FileChannel
            FileInputStream fis = new FileInputStream("testWrite.txt");
            FileChannel channel = fis.getChannel();

            // 创建ByteBuffer来读取double字节数组
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 读取数据到ByteBuffer
            int bytesRead = channel.read(buffer);
            buffer.flip(); // 切换为读模式

            // 创建DoubleBuffer来读取double数据
            DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();

            // 逐个读取double数据
            while (doubleBuffer.hasRemaining()) {
                double number = doubleBuffer.get();
                System.out.println(number); // 输出读取的double数据
            }

            // 关闭通道和流
            channel.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}