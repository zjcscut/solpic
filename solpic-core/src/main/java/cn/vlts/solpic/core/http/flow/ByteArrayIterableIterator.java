package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.util.IoUtils;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Byte array iterable iterator.
 *
 * @author throwable
 * @since 2024/7/31 00:14
 */
public class ByteArrayIterableIterator implements Iterator<ByteBuffer> {

    private final Iterable<? extends byte[]> iterable;

    private final Supplier<ByteBuffer> bufSupplier;

    private ByteBuffer buf;

    private volatile boolean finished;

    private volatile boolean haveNext = true;

    public ByteArrayIterableIterator(Iterable<? extends byte[]> iterable) {
        this(iterable, IoUtils.X::newByteBuffer);
    }

    public ByteArrayIterableIterator(Iterable<? extends byte[]> iterable, Supplier<ByteBuffer> bufSupplier) {
        this.iterable = iterable;
        this.bufSupplier = bufSupplier;
    }

    // TODO impl this method.
    @Override
    public boolean hasNext() {
        if (finished) {
            return false;
        }
        if (!haveNext) {
            return false;
        }

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
