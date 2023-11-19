package cn.edu.tongji.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiveFile {
    private final DataInputStream dataInputStream;
    private final String basePath;
    private final int fileNameLength;
    private static final int BUFFER_SIZE = 4096;
    public ReceiveFile(int fileNameLength, String basePath, DataInputStream dataInputStream) {
        this.fileNameLength = fileNameLength;
        this.basePath = basePath;
        this.dataInputStream = dataInputStream;
    }

    // receive the file and write into the target folder
    public int receive() throws Exception {
        // get the file name
        byte[] fileNameByte = new byte[fileNameLength];
        dataInputStream.readFully(fileNameByte);
        String fileName = new String(fileNameByte, StandardCharsets.UTF_8);
        // get the length of the file content
        long fileLength = dataInputStream.readLong();
        // create the folder if not exists
        Path filePath = Paths.get(basePath, fileName);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        // get the content of the file as writing into the local file
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // set the buffer
            byte[] buffer = new byte[BUFFER_SIZE];
            // record the number of bytes have read
            int totalBytesRead = 0;
            while (totalBytesRead < fileLength) {
                int bytesRead = dataInputStream.read(buffer, 0, Math.min(buffer.length, (int) (fileLength - totalBytesRead)));
                // get nothing from the socket
                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
                // write into the local file
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                fileChannel.write(byteBuffer);
            }
        } catch (Exception e) {
            System.out.println("receive file chunk failed " + filePath);
            // sth goes wrong then delete the destroyed file chunk
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ex) {
                System.out.println("destroyed file delete failed " + filePath);
            }
            // further propagation of the exception info
            throw e;
        }

        // extract the chunk index from the file name
        return getChunkIndex(fileName);
    }

    private int getChunkIndex(String fileName) throws Exception {
        String regex = "\\$(\\d+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);
        // get the last $
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1);
        }
        if (lastMatch != null) {
            int index = Integer.parseInt(lastMatch);
            return index;
        } else {
            throw new Exception("file name format error");
        }
    }

}
