import java.io.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class NumPair {
    public static final long SEED = 244;  //随机数种子
    // 7639999: 2038对
    // 48968399: 2037对
    // 74968336: 2138对
    // 158276472: 2221对
    // 246531038: 2324对
    // 811697185: 2478对
    public static final int CACHE_SIZE = 0x40000;  //缓冲区大小，默认256KB
    public static ByteBuffer[] caches = new ByteBuffer[3];  //存放double类型数的缓冲区
    public static File[] files = new File[4];  //目标文件和存放三组数对的文件
    public static RandomAccessFile[] rFiles = new RandomAccessFile[4];  //四个文件的随机访问对象

    /**
     * 将存放某一组数对的文件内容复制到目标文件
     * @param srcFileNum 数对组号
     */
    public static void copyFile(final int srcFileNum) {
        //文件顺序读取，用BufferedInputStream提高效率
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(files[srcFileNum + 1]))) {
            byte[] buffer = new byte[CACHE_SIZE];  //缓冲区
            int bytesRead;  //读取的字节数

            while ((bytesRead = reader.read(buffer)) != -1) {
                rFiles[0].write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            System.out.println("复制文件data" + (srcFileNum + 1) + "内容时发生错误");
        }
    }

    /**
     * 将数对写入缓冲区（缓冲区满则写入文件）
     * @param group 数对所属组号
     * @param num1 第一个数
     * @param num2 第二个数
     */
    public static void inCache(final int group, final double num1, final double num2) {
        try {
            //将两个数写入缓冲区
            caches[group].putDouble(num1);
            caches[group].putDouble(num2);

            //每次写完检查，若缓冲区满了则写入文件
            if (caches[group].position() == CACHE_SIZE) {
                rFiles[group + 1].write(caches[group].array());
                caches[group].clear();
            }
        } catch (IOException e) {
            System.out.println("缓冲区" + group + "写入文件时发生错误");
        }
    }

    /**
     * 将缓冲区内容写入文件
     * @param group 缓冲区对应的数对组号
     */
    public static void flushCache(final int group) {
        try {
            byte[] remain = new byte[caches[group].position()];  //创建缓冲区剩余数据长度的数组
            caches[group].position(0);  //复位缓冲区指针，以便读取
            caches[group].get(remain);             //读取数据
            rFiles[group + 1].write(remain);       //rFiles有4个，下标需加1
        } catch (IOException e) {
            System.out.println("缓冲区" + group + "清空时发生错误");
        }
    }

    public static void main(String[] args) {
        //用种子创建生成器
        Random generator = new Random(SEED);

        //初始化内存缓冲区
        for (int i = 0; i < 3; i++) {
            caches[i] = ByteBuffer.allocate(CACHE_SIZE);
        }

        //用随机访问方式打开文件
        try {
            for (int i = 0; i < 4; i++) {
                files[i] = new File("./q1/data" + (i > 0 ? String.valueOf(i) : "") + ".dat");

                if (!files[i].createNewFile()) {
                    System.out.println("文件data" + i + ".dat创建失败，请重试！");
                }

                rFiles[i] = new RandomAccessFile(files[i], "rw");
            }

            //清空文件
            rFiles[0].setLength(0);

            //定义所需变量
            int gpCount1 = 0, gpStart1 = 32;       //group1数对个数，group1起始位置
            int gpCount2 = 0, gpStart2 = 32;       //group2数对个数，group2起始位置
            int gpCount3 = 0, gpStart3 = 32;       //group3数对个数，group3起始位置
            int halfCount = 0, halfAllCount = 0;   //超过0.5数值个数，超过0.5数对个数

            //开始生成和写入计时
            final long startTime = System.currentTimeMillis();

            while (true) {
                final double num1 = generator.nextDouble();  //生成数对第1个数
                final double num2 = generator.nextDouble();  //生成数对第2个数

                if (num1 > 0.9999 && num2 > 0.9999) {  //退出
                    break;
                }
                else if (num1 < 0.46 && num2 < 0.46) {  //放入第一组
                    //第一组计数+1
                    gpCount1++;

                    //第二组起始位置也是第一组末尾位置，在此将数值以二进制形式插入，同时修改第二、第三组起始位置
                    inCache(0, num1, num2);
                    gpStart2 += (Double.BYTES << 1);
                    gpStart3 += (Double.BYTES << 1);
                }
                else if (num1 > 0.72 && num2 > 0.72) {  //放入第三组
                    //第三组计数+1
                    gpCount3++;

                    //文件末尾位置也是第三组末尾位置，在此将数值以二进制形式插入，且不用修改起始位置
                    inCache(2, num1, num2);
                }
                else {  //放入第二组
                    //第二组计数+1
                    gpCount2++;

                    //第三组起始位置也是第二组末尾位置，在此将数值以二进制形式插入，同时修改第三组起始位置
                    inCache(1, num1, num2);
                    gpStart3 += (Double.BYTES << 1);
                }

                //根据数对值进行统计
                final boolean flag1 = num1 > 0.5;
                final boolean flag2 = num2 > 0.5;
                if (flag1 && flag2) {  //2个都大于0.5
                    halfCount += 2;
                    halfAllCount += 1;
                }
                else if (flag1 || flag2) {  //只有一个大于0.5
                    halfCount++;
                }
            }

            //插入文件头
            rFiles[0].writeInt(gpCount1 + gpCount2 + gpCount3);
            rFiles[0].writeInt(gpCount1);
            rFiles[0].writeInt(gpStart1);
            rFiles[0].writeInt(gpCount2);
            rFiles[0].writeInt(gpStart2);
            rFiles[0].writeInt(gpCount3);
            rFiles[0].writeInt(gpStart3);
            rFiles[0].writeInt(0);
            //将缓冲区剩余内容写入对应文件，并把对应文件内容复制到目标文件
            for (int i = 0; i < 3; i++) {
                flushCache(i);
                copyFile(i);
            }

            //计时结束
            final long endTime = System.currentTimeMillis();

            //打印统计信息
            System.out.println("halfCount: " + halfCount);
            System.out.println("halfAllCount: " + halfAllCount);
            System.out.println("time: " + (endTime - startTime) + "ms");

            //关闭随机访问资源，并删除三个临时文件
            for (int i = 0; i < 4; i++) {
                rFiles[i].close();

                if (i > 0 && !files[i].delete())
                    System.out.println("文件data" + i + ".dat删除未成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
