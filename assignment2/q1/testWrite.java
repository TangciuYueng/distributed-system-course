import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileWritingExample {
    public static void main(String[] args) {
        try {
            // 创建FileOutputStream和FileChannel
            FileOutputStream fos = new FileOutputStream("testWrite.bat");
            FileChannel channel = fos.getChannel();

            // 设置通道位置为56
            channel.position(56);

            // 将数据写入通道
            String data = "Hello, World!";
            ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
            channel.write(buffer);



            // 创建要写入的7个 double 数字
            double[] numbers = {1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7};
            ByteBuffer doubleBuffer = ByteBuffer.allocate(numbers.length * Double.BYTES);
            for (double number : numbers) {
                doubleBuffer.putDouble(number);
            }
            doubleBuffer.flip();

            channel.position(0);
            channel.write(doubleBuffer);

            // 关闭通道和流
            channel.close();
            fos.close();

            System.out.println("数据写入成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}