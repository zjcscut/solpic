package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.util.IoUtils;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Byte array iterable iterator.
 *
 * @author throwable
 * @since 2024/7/31 00:14
 */
public class ByteArrayIterableIterator implements Iterator<ByteBuffer> {

    private final Iterator<? extends byte[]> iterator;

    private final Supplier<ByteBuffer> bufSupplier;

    private final ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();

    public ByteArrayIterableIterator(Iterable<? extends byte[]> iterable) {
        this(iterable, IoUtils.X::newByteBuffer);
    }

    public ByteArrayIterableIterator(Iterable<? extends byte[]> iterable, Supplier<ByteBuffer> bufSupplier) {
        this.iterator = iterable.iterator();
        this.bufSupplier = bufSupplier;
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty() || iterator.hasNext();
    }

    @Override
    public ByteBuffer next() {
        ByteBuffer buffer = queue.poll();
        while (Objects.isNull(buffer)) {
            copy();
            buffer = queue.poll();
        }
        return buffer;
    }

    private void copy() {
        byte[] bytes = iterator.next();
        int length = bytes.length;
        if (length == 0 && iterator.hasNext()) {
            return;
        }
        int offset = 0;
        do {
            ByteBuffer b = bufSupplier.get();
            int max = b.capacity();
            int toCopy = Math.min(max, length);
            b.put(bytes, offset, toCopy);
            offset += toCopy;
            length -= toCopy;
            b.flip();
            queue.add(b);
        } while (length > 0);
    }
}
