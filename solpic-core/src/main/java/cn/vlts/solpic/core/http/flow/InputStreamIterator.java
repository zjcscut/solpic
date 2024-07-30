package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * InputStream iterator.
 *
 * @author throwable
 * @since 2024/7/30 21:57
 */
public class InputStreamIterator implements Iterator<ByteBuffer> {

    private final InputStream inputStream;

    private final Supplier<ByteBuffer> bufSupplier;

    private ByteBuffer buf;

    private volatile boolean isEof;

    private volatile boolean haveNext = true;

    public InputStreamIterator(InputStream inputStream) {
        this(inputStream, IoUtils.X::newByteBuffer);
    }

    public InputStreamIterator(InputStream inputStream, Supplier<ByteBuffer> bufSupplier) {
        this.inputStream = inputStream;
        this.bufSupplier = bufSupplier;
    }

    @Override
    public boolean hasNext() {
        if (isEof) {
            return false;
        }
        if (!haveNext) {
            return false;
        }
        buf = bufSupplier.get();
        buf.clear();
        int bytesRead;
        try {
            bytesRead = inputStream.read(buf.array(), buf.arrayOffset(), buf.capacity());
            if (-1 == bytesRead) {
                isEof = true;
                return haveNext = false;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (!haveNext) {
                IoUtils.X.closeQuietly(inputStream);
            }
        }
        buf.limit(bytesRead).position(0);
        return haveNext = true;
    }

    @Override
    public ByteBuffer next() {
        if (!haveNext) {
            throw new NoSuchElementException();
        }
        return buf;
    }
}
