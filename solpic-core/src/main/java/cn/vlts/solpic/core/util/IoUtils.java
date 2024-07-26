package cn.vlts.solpic.core.util;

import java.io.*;

/**
 * IO utils.
 *
 * @author throwable
 * @since 2024/7/26 星期五 11:10
 */
public enum IoUtils {
    X;

    private static final int READ_BUF_SIZE = 4 * 1024;

    private static final int WRITE_BUF_SIZE = 4 * 1024;

    public byte[] readBytes(InputStream in, boolean shouldClose) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(WRITE_BUF_SIZE);
            int bc = 0;
            int br;
            for (byte[] buf = new byte[READ_BUF_SIZE]; (br = in.read(buf)) != -1; bc += br) {
                bos.write(buf, 0, br);
            }
            bos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (shouldClose) {
                try {
                    in.close();
                } catch (Exception ignore) {

                }
            }
        }
    }
}
