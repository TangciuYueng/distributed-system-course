import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.io.RandomAccessFile;

public class RandomNumberWriter {
    public static void main(String[] args) {
        String filePath1 = "h2q1.dat";
        String filePath2 = "hw1.group2.dat";
        String filePath3 = "hw1.group3.dat";
        // 缓冲区大小 设置为1页
        int bufferSize = 1024 * 8;
        // 记录组内数对个数
        int group1Size = 0;
        int group2Size = 0;
        int group3Size = 0;
        // 超过0.5的数的个数
        int overHalfCount = 0;
        // 超过0.5的数对个数
        int doubleOverHalfCount = 0;
        // 文件头字节4 * 8 = 32
        int headerSize = 32;
        // 阈值
        final double threshold1 = 0.46;
        final double threshold2 = 0.72;


        long startTime = System.currentTimeMillis();

        try (RandomAccessFile fos1 = new RandomAccessFile(filePath1, "rw");
                RandomAccessFile fos2 = new RandomAccessFile(filePath2, "rw");
                RandomAccessFile fos3 = new RandomAccessFile(filePath3, "rw");
                FileChannel fileChannel1 = fos1.getChannel();
                FileChannel fileChannel2 = fos2.getChannel();
                FileChannel fileChannel3 = fos3.getChannel()) {

            ByteBuffer buffer1 = ByteBuffer.allocate(bufferSize);
            ByteBuffer buffer2 = ByteBuffer.allocate(bufferSize);
            ByteBuffer buffer3 = ByteBuffer.allocate(bufferSize);
            // 设置随机数种子
            Random random = new Random(244);

            // 设置起始写入位置，预留文件头位置
            fileChannel1.position(headerSize);

            while (true) {
                double d1 = random.nextDouble();
                double d2 = random.nextDouble();

                // 循环终止条件
                if (d1 > 0.9999 && d2 > 0.9999) {
                    break;
                }
                // 记录打印结果
                if (d1 > 0.5) {
                    overHalfCount++;
                }
                if (d2 > 0.5) {
                    overHalfCount++;
                }
                if (d1 > 0.5 && d2 > 0.5) {
                    doubleOverHalfCount++;
                }
                // 分组存入
                if (d1 < threshold1 && d2 < threshold1) {
                    buffer1.putDouble(d1);
                    buffer1.putDouble(d2);
                    group1Size++;
                } else if (d1 > threshold2 && d2 > threshold2) {
                    buffer3.putDouble(d1);
                    buffer3.putDouble(d2);
                    group3Size++;
                } else {
                    buffer2.putDouble(d1);
                    buffer2.putDouble(d2);
                    group2Size++;
                }

                // 当缓冲区剩余空间不足16字节（每个double占8字节）时，进行写入操作
                if (buffer1.remaining() < 16) {
                    buffer1.flip();
                    // 写入文件
                    fileChannel1.write(buffer1);
                    // 清空缓冲区
                    buffer1.clear();
                }
                // 当缓冲区剩余空间不足16字节（每个double占8字节）时，进行写入操作
                if (buffer2.remaining() < 16) {
                    buffer2.flip();
                    // 写入文件
                    fileChannel1.write(buffer2);
                    // 清空缓冲区
                    buffer2.clear();
                }
                // 当缓冲区剩余空间不足16字节（每个double占8字节）时，进行写入操作
                if (buffer3.remaining() < 16) {
                    buffer3.flip();
                    // 写入文件
                    fileChannel3.write(buffer3);
                    // 清空缓冲区
                    buffer3.clear();
                }
            }

            // 检查是否还有未写入的数据
            if (buffer1.hasRemaining()) {
                buffer1.flip();
                // 写入文件
                fileChannel1.write(buffer1);
                // 清空缓冲区
                buffer1.clear();
            }
            if (buffer2.hasRemaining()) {
                buffer2.flip();
                // 写入文件
                fileChannel2.write(buffer2);
                // 清空缓冲区
                buffer2.clear();
            }
            if (buffer3.hasRemaining()) {
                buffer3.flip();
                // 写入文件
                fileChannel3.write(buffer3);
                // 清空缓冲区
                buffer3.clear();
            }

            // 追加复制group2.dat到group1.dat
            fileChannel1.position(fileChannel1.size());
            fileChannel2.transferTo(0, fileChannel2.size(), fileChannel1);

            // 追加复制group3.dat到group1.dat
            fileChannel1.position(fileChannel1.size());
            fileChannel3.transferTo(0, fileChannel3.size(), fileChannel1);

            // 从头开始写 不更换缓冲区 直接写入文件头
            fileChannel1.position(0);
            writeHeaderToFile(fileChannel1, buffer1, group1Size, group2Size, group3Size);

            long endTime = System.currentTimeMillis();

            System.out.println("用时: " + (endTime - startTime) + "ms");
            System.out.println("大于0.5的数值的个数: " + overHalfCount);
            System.out.println("均大于0.5的数对个数: " + doubleOverHalfCount);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHeaderToFile(FileChannel fileChannel, ByteBuffer buffer, 
                                        int group1Size, int group2Size, int group3Size) throws IOException  {
        int total = group1Size + group2Size + group3Size;
        int startPosition1 = 32;
        int startPosition2 = startPosition1 + group1Size * 16;
        int startPosition3 = startPosition2 + group2Size * 16;

        buffer.putInt(total);
        buffer.putInt(group1Size);
        buffer.putInt(startPosition1);
        buffer.putInt(group2Size);
        buffer.putInt(startPosition2);
        buffer.putInt(group3Size);
        buffer.putInt(startPosition3);

        buffer.flip();
        // 写入文件
        fileChannel.write(buffer);
        // 清空缓冲区
        buffer.clear();
    }
}