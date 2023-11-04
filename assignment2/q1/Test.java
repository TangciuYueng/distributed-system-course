import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Test {
    public static final int BUFFER_SIZE = 0x4000;
    public static byte[] outBuffer = new byte[BUFFER_SIZE];
    public static byte[] inBuffer = new byte[BUFFER_SIZE];
    public static int outPointer = 0;
    public static int inPointer = 0;
    public static RandomAccessFile in;
    public static FileOutputStream out;
    public static int[] groupCount = new int[3];
    public static int[] groupPassCount = new int[4];
    public static int[] groupStart = new int[3];
    public static double[] groupFirst = new double[3];
    public static final String inFilePath = "h2q1.dat";
    public static final String outFilePath = "output.txt";

    public static int outBufferStore(final int value) {
        final byte[] bytes = String.valueOf(value).getBytes();

        try {
            if (bytes.length + 1 > BUFFER_SIZE - outPointer) {
                out.write(outBuffer, 0, outPointer);
                outPointer = 0;
            }

            for (byte b: bytes) {
                outBuffer[outPointer++] = b;
            }
            outBuffer[outPointer++] = '\n';
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    public static double outBufferStore(final double value) {
        final byte[] bytes = String.valueOf(value).getBytes();

        try {
            if (bytes.length > BUFFER_SIZE - outPointer) {
                out.write(outBuffer, 0, outPointer);
                outPointer = 0;
            }

            for (byte b: bytes) {
                outBuffer[outPointer++] = b;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    public static void outBufferStore(final double value1, final double value2) {
        final byte[] bytes1 = String.valueOf(value1).getBytes();
        final byte[] bytes2 = String.valueOf(value2).getBytes();

        try {
            if (bytes1.length + bytes2.length + 3 > BUFFER_SIZE - outPointer) {
                out.write(outBuffer, 0, outPointer);
                outPointer = 0;
            }

            for (byte b : bytes1) {
                outBuffer[outPointer++] = b;
            }
            outBuffer[outPointer++] = (byte) ',';
            outBuffer[outPointer++] = (byte) ' ';
            for (byte b: bytes2) {
                outBuffer[outPointer++] = b;
            }
            outBuffer[outPointer++] = (byte) '\n';
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outBufferStore(String text) {
        final byte[] bytes = text.getBytes();

        try {
            if (bytes.length > BUFFER_SIZE - outPointer) {
                out.write(outBuffer, 0, outPointer);
                outPointer = 0;
            }

            for (byte b: bytes) {
                outBuffer[outPointer++] = b;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void flushOutBuffer() {
        try {
            out.write(outBuffer, 0, outPointer);
            outPointer = 0;
        } catch (IOException e) {
            System.out.println("缓冲区清空时发生错误");
        }
    }

    public static double bytesToDouble(byte[] bytes) {
        if (bytes.length != 8)
            return 0.0;

        long middle = 0;

        for (int i = 0; i < 8; i++) {
            middle = (middle << 8) | (bytes[i] & 0xFF);
        }

        return Double.longBitsToDouble(middle);
    }

    public static int checkPairGroup(final double num1, final double num2) {
        if (num1 < 0 || num1 > 1 || num2 < 0 || num2 > 1)
            return 0;
        else if (num1 >= 0 && num1 < 0.46 && num2 >= 0 && num2 < 0.46)
            return 1;
        else if (num1 > 0.72 && num2 > 0.72)
            return 3;
        else
            return 2;
    }

    public static void inBufferStore(final int groupNum) {
        try {
            outBufferStore("\n********************************************Group" + (groupNum + 1) + "********************************************\n\n");
            final int maxRead = groupCount[groupNum] * 2 * Double.BYTES;
            int remainRead = maxRead;
            byte[] bytesNum1 = new byte[8];
            byte[] bytesNum2 = new byte[8];

            while (remainRead != 0) {
                final int readLen = Math.min(remainRead, BUFFER_SIZE);  //在该组区域内尝试读BUFFER_SIZE个字节
                in.read(inBuffer, 0, readLen);
                inPointer = 0;

                for (int i = 0; i < readLen / Double.BYTES / 2; i++) {  //计算出本次读取的数对个数
                    for (int j = 0; j < 8; j++) {
                        bytesNum1[j] = inBuffer[inPointer++];
                    }
                    for (int j = 0; j < 8; j++) {
                        bytesNum2[j] = inBuffer[inPointer++];
                    }

                    final double num1 = bytesToDouble(bytesNum1);
                    final double num2 = bytesToDouble(bytesNum2);
                    groupPassCount[checkPairGroup(num1, num2)]++;

                    if (remainRead == maxRead && i == 0)
                        groupFirst[groupNum] = num1;

                    outBufferStore(num1, num2);
                }

                remainRead -= readLen;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkFirstNum(final int groupNum) {
        try {
            in.seek(groupStart[groupNum]);
            System.out.print("第" + (groupNum + 1) + "组开始位置：" + groupStart[groupNum] + " 的第一个数为：");
            final double realFirst = in.readDouble();
            System.out.print(realFirst);
            System.out.println(realFirst == groupFirst[groupNum] ? "，位置正确" : "，位置错误");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkPairPass() {
        for (int i = 0; i < 3; i++) {
            System.out.println("第" + (i + 1) + "组预计有" + groupCount[i] + "对，实际通过" + groupPassCount[i + 1] + "对");
        }

        System.out.println("完全非法的数对有：" + groupPassCount[0] + "对");
    }

    public static void main(String[] args) {
        try {
            in = new RandomAccessFile(inFilePath, "r");
            out = new FileOutputStream(outFilePath);
            for (int i = 0; i < 4; i++) {
                groupPassCount[i] = 0;
            }
            final long startTime = System.currentTimeMillis();

            final int groupCountAll = outBufferStore(in.readInt());

            for (int i = 0; i < 3; i++) {
                groupCount[i] = outBufferStore(in.readInt());
                groupStart[i] = outBufferStore(in.readInt());
            }

            final int empty = outBufferStore(in.readInt());

            for (int i = 0; i < 3; i++) {
                inBufferStore(i);
            }

            System.out.println("*******************************************检测信息*******************************************");
            for (int i = 0; i < 3; i++) {
                checkFirstNum(i);
            }

            checkPairPass();
            flushOutBuffer();

            System.out.println("用时" + (System.currentTimeMillis() - startTime) + "ms");

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
