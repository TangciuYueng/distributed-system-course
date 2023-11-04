import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.RandomAccessFile;

public class FileChannelDemo {

	// FileChannel 读取数据到 buffer 中
	public static void main(String[] args) throws IOException {
		// 创建 FileChannel
		RandomAccessFile accessFile = new RandomAccessFile("test.txt", "rw");
		FileChannel fileChannel = accessFile.getChannel();

		// 创建 buffer
		ByteBuffer byteBuffer = ByteBuffer.allocate(7);

		while (fileChannel.read(byteBuffer) != -1) {
			System.out.println("读取到了： " + byteBuffer);
			byteBuffer.flip();
			while (byteBuffer.hasRemaining()) {
				System.out.println((char) byteBuffer.get() + "$");
			}
			byteBuffer.clear();
            System.out.println(fileChannel.position() + "aaaaaaaa");
            System.out.println("重新设置position");
            // fileChannel.position(3);
		}
		fileChannel.close();
		System.out.println("end");
	}
}
