package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Pulling InputStream.
 *
 * @author throwable
 * @since 2024/8/3 10:50
 */
public class PullingInputStream extends InputStream {

    private final ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();

    private volatile boolean closed;

    private volatile ByteBuffer buffer;

    private final Subscription subscription;

    public PullingInputStream(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        if (Objects.isNull(buffer) || !buffer.hasRemaining()) {
            tryRequestMore();
            buffer = queue.poll();
            if (Objects.isNull(buffer)) {
                return -1;
            }
        }
        return buffer.get() & 0xff;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        queue.clear();
    }

    public void offer(ByteBuffer byteBuffer) throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        queue.offer(byteBuffer);
    }

    private void tryRequestMore() {
        if (!closed && (Objects.isNull(buffer) || queue.isEmpty())) {
            subscription.request(1);
        }
    }
}
