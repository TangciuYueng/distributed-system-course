import java.io.*;

public class Test {
    public static final int BUFFER_SIZE = 0x4000;
    public static byte[] inBuffer = new byte[BUFFER_SIZE];
    public static int inPointer = 0;
    public static BufferedInputStream inB;
    public static RandomAccessFile inR;
    public static BufferedWriter out;
    public static int[] groupCount = new int[3];
    public static int[] groupPassCount = new int[4];
    public static int[] groupStart = new int[3];
    public static double[] groupFirst = new double[3];
    public static final String inFilePath = "./q1/data.dat";
    public static final String outFilePath = "./q1/test.txt";
    public enum NumKind { err, low, mid, high }

    public static String formatSize(final long size) {
        if (size >= 0 &&size < 0x400)
            return size + "B";
        else if (size < 0x100000)
            return String.format("%.2f", ((double) size / 0x400)) + "KB";
        else if (size < 0x40000000)
            return String.format("%.2f", ((double) size / 0x100000)) + "MB";
        else
            return String.format("%.2f", ((double) size / 0x40000000)) + "GB";
    }

    public static double bytesToDouble(byte[] bytes) {
        long middle = 0;

        for (byte b: bytes) {
            middle = (middle << 8) | (b & 0xFF);
        }

        return Double.longBitsToDouble(middle);
    }

    public static int bytesToInteger(int bytesRead, byte[] bytes) {
        if (bytesRead != 4)
            return -1;

        int ret = 0;

        for (byte b: bytes) {
            ret = (ret << 8) | (b & 0xFF);
        }

        return ret;
    }

    public static int checkPairGroup(final double num1, final double num2) {
        NumKind kind1, kind2;

        if (num1 >= 0 && num1 < 0.46)
            kind1 = NumKind.low;
        else if (num1 >= 0.46 && num1 <= 0.72)
            kind1 = NumKind.mid;
        else if (num1 > 0.72 && num1 <= 1)
            kind1 = NumKind.high;
        else
            kind1 = NumKind.err;

        if (num2 >= 0 && num2 < 0.46)
            kind2 = NumKind.low;
        else if (num2 >= 0.46 && num2 <= 0.72)
            kind2 = NumKind.mid;
        else if (num2 > 0.72 && num2 <= 1)
            kind2 = NumKind.high;
        else
            kind2 = NumKind.err;

        if (kind1 == NumKind.err || kind2 == NumKind.err)
            return 0;
        else if (kind1 == NumKind.low && kind2 == NumKind.low)
            return 1;
        else if (kind1 == NumKind.high && kind2 == NumKind.high)
            return 3;
        else
            return 2;
    }

    public static void checkGroup(final int groupNum) {
        try {
            out.write("\n********************************************Group" + (groupNum + 1) + "********************************************\n\n");
            final int maxRead = groupCount[groupNum] * 2 * Double.BYTES;
            int remainRead = maxRead;
            byte[] bytesNum1 = new byte[8];
            byte[] bytesNum2 = new byte[8];

            while (remainRead != 0) {
                final int readLen = Math.min(remainRead, BUFFER_SIZE);  //在该组区域内尝试读BUFFER_SIZE个字节
                final int bytesRead = inB.read(inBuffer, 0, readLen);
                inPointer = 0;

                for (int i = 0; i < readLen / Double.BYTES / 2; i++) {  //计算出本次读取的数对个数
                    System.arraycopy(inBuffer, inPointer, bytesNum1, 0, 8);
                    inPointer += 8;
                    System.arraycopy(inBuffer, inPointer, bytesNum2, 0, 8);
                    inPointer += 8;

                    final double num1 = bytesToDouble(bytesNum1);
                    final double num2 = bytesToDouble(bytesNum2);
                    groupPassCount[checkPairGroup(num1, num2)]++;

                    if (remainRead == maxRead && i == 0)
                        groupFirst[groupNum] = num1;

                    out.write(num1 + ", " + num2 + '\n');
                }

                remainRead -= bytesRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkFirstNum() {
        for (int i = 0; i < 3; i++) {
            if (groupCount[i] == 0) {
                System.out.println("第" + (i + 1) + "组为空，未检查本组开始位置");
                continue;
            }

            try {
                inR.seek(groupStart[i]);
                System.out.print("第" + (i + 1) + "组开始位置：" + groupStart[i] + " 的第一个数为：");
                final double realFirst = inR.readDouble();
                System.out.print(realFirst);
                System.out.println(realFirst == groupFirst[i] ? "，位置正确" : "，位置错误");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void checkPairPass() {
        for (int i = 0; i < 3; i++) {
            if (groupCount[i] == 0)
                System.out.println("第" + (i + 1) + "组为空，未检查数对情况");
            else
                System.out.println("第" + (i + 1) + "组预计有" + groupCount[i] + "对，实际通过" + groupPassCount[i + 1] + "对");
        }

        System.out.println("完全非法的数对有：" + groupPassCount[0] + "对");
    }

    public static void main(String[] args) {
        try {
            File inFile = new File(inFilePath);
            File outFile = new File(outFilePath);
            inB = new BufferedInputStream(new FileInputStream(inFile), BUFFER_SIZE);
            inR = new RandomAccessFile(inFile, "r");
            out = new BufferedWriter(new FileWriter(outFile), BUFFER_SIZE);
            for (int i = 0; i < 4; i++) {
                groupPassCount[i] = 0;
            }
            final long startTime = System.currentTimeMillis();
            byte[] intBytes = new byte[4];

            final int groupCountAll = bytesToInteger(inB.read(intBytes), intBytes);
            out.write(groupCountAll + "\n");

            for (int i = 0; i < 3; i++) {
                groupCount[i] = bytesToInteger(inB.read(intBytes), intBytes);
                out.write(groupCount[i] + "\n");
                groupStart[i] = bytesToInteger(inB.read(intBytes), intBytes);
                out.write(groupStart[i] + "\n");
            }

            final int empty = bytesToInteger(inB.read(intBytes), intBytes);
            out.write(empty + "\n");

            for (int i = 0; i < 3; i++) {
                checkGroup(i);
            }
            out.flush();

            System.out.println("*******************************************基本信息*******************************************");
            System.out.println("总数：" + groupCountAll + "对");
            for (int i = 0; i < 3; i++) {
                System.out.println("第" + (i + 1) + "组：" + groupCount[i] + "对");
                System.out.println("第" + (i + 1) + "组起始位置：" + groupStart[i]);
            }
            System.out.println("数据文件大小：" + formatSize(inFile.length()));
            System.out.println("**********************************************************************************************");

            System.out.println("*******************************************检测信息*******************************************");
            checkFirstNum();
            System.out.println();
            checkPairPass();
            System.out.println("生成文件大小：" + formatSize(outFile.length()));
            System.out.println("\n用时：" + (System.currentTimeMillis() - startTime) + "ms");
            System.out.println("**********************************************************************************************");

            inB.close();
            inR.close();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
