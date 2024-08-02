package cn.vlts.solpic.core.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * IO utils.
 *
 * @author throwable
 * @since 2024/7/26 星期五 11:10
 */
public enum IoUtils {
    X;

    public static int READ_BUF_SIZE = 8 * 1024;

    public static int WRITE_BUF_SIZE = 8 * 1024;

    static {
        try {
            String readBufSizeProperty = System.getProperty("solpic.http.read.buffer.size");
            if (Objects.nonNull(readBufSizeProperty)) {
                READ_BUF_SIZE = Integer.parseInt(readBufSizeProperty);
            }
        } catch (Throwable ignore) {

        }
        try {
            String writeBufSizeProperty = System.getProperty("solpic.http.write.buffer.size");
            if (Objects.nonNull(writeBufSizeProperty)) {
                WRITE_BUF_SIZE = Integer.parseInt(writeBufSizeProperty);
            }
        } catch (Throwable ignore) {

        }
    }

    public void closeQuietly(Closeable closeable) {
        if (Objects.nonNull(closeable)) {
            try {
                closeable.close();
            } catch (IOException ignore) {

            }
        }
    }

    public ByteBuffer newReadByteBuffer() {
        return ByteBuffer.allocate(READ_BUF_SIZE);
    }

    public ByteBuffer newReadByteBuffer(int bufSize) {
        return ByteBuffer.allocate(bufSize);
    }

    public BufferedReader newBufferedReader(Reader reader) {
        return new BufferedReader(reader, READ_BUF_SIZE);
    }

    public InputStream copyByteArrayToInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

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

    public List<ByteBuffer> copyByteArrayToByteBuffers(byte[] content) {
        return copyByteArrayToByteBuffers(content, READ_BUF_SIZE);
    }

    public List<ByteBuffer> copyByteArrayToByteBuffers(byte[] content, int bufSize) {
        return copyByteArrayToByteBuffers(content, 0, content.length, bufSize);
    }

    public List<ByteBuffer> copyByteArrayToByteBuffers(byte[] content, int offset, int length, int bufSize) {
        return copyByteArrayToByteBuffers(content, offset, length, () -> ByteBuffer.allocate(bufSize));
    }

    public List<ByteBuffer> copyByteArrayToByteBuffers(byte[] content, int offset, int length, Supplier<ByteBuffer> supplier) {
        List<ByteBuffer> buffers = new ArrayList<>();
        while (length > 0) {
            ByteBuffer buf = supplier.get();
            int max = buf.capacity();
            int toCopy = Math.min(max, length);
            buf.put(content, offset, toCopy);
            offset += toCopy;
            length -= toCopy;
            buf.flip();
            buffers.add(buf);
        }
        return buffers;
    }

    public byte[] copyByteBuffersToByteArray(List<ByteBuffer> bufferList) {
        return copyByteBuffersToByteArray(bufferList, WRITE_BUF_SIZE);
    }

    public byte[] copyByteBuffersToByteArray(List<ByteBuffer> bufferList, int bufSize) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bufSize);
        for (ByteBuffer buffer : bufferList) {
            if (buffer.hasRemaining()) {
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                try {
                    bos.write(bytes);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return bos.toByteArray();
    }

    /**
     * only used for heap ByteBuffers.
     */
    public byte[] fastCopyByteBuffersToByteArray(List<ByteBuffer> bufferList) {
        if (Objects.isNull(bufferList)) {
            return new byte[0];
        }
        int total = 0;
        for (ByteBuffer buffer : bufferList) {
            total += buffer.remaining();
        }
        byte[] result = new byte[total];
        int offset = 0;
        for (ByteBuffer buffer : bufferList) {
            int length = buffer.remaining();
            System.arraycopy(buffer.array(), buffer.position(), result, offset, length);
            offset += length;
        }
        return result;
    }
}
