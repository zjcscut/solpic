package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ByteBuffer consumer OutputStream.
 *
 * @author throwable
 * @since 2024/8/1 星期四 16:35
 */
public class ByteBufferConsumerOutputStream extends OutputStream {

    private final ByteBuffer buf;

    private final Consumer<ByteBuffer> consumer;

    public ByteBufferConsumerOutputStream(Consumer<ByteBuffer> consumer) {
        this(IoUtils.X::newReadByteBuffer, consumer);
    }

    public ByteBufferConsumerOutputStream(int bufSize, Consumer<ByteBuffer> consumer) {
        this(() -> IoUtils.X.newReadByteBuffer(bufSize), consumer);
    }

    public ByteBufferConsumerOutputStream(Supplier<ByteBuffer> supplier,
                                          Consumer<ByteBuffer> consumer) {
        this.buf = supplier.get();
        this.consumer = consumer;
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(bytes[off + i]);
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (!buf.hasRemaining()) {
            flushBuffer();
        }
        buf.put((byte) b);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        flushBuffer();
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    private void flushBuffer() throws IOException {
        if (buf.position() > 0) {
            buf.flip();
            consumer.accept(buf);
            buf.clear();
        }
    }
}
