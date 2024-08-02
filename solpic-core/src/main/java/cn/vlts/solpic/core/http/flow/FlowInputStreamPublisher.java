package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.Publisher;
import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Flow InputStream publisher.
 *
 * @author throwable
 * @since 2024/8/2 星期五 11:36
 */
public final class FlowInputStreamPublisher implements Publisher<List<ByteBuffer>> {

    private final InputStream inputStream;

    private final Supplier<ByteBuffer> supplier;

    private Subscriber<? super List<ByteBuffer>> subscriber;

    private final FlowInputStreamSubscription subscription = new FlowInputStreamSubscription();

    private volatile boolean canceled;

    public static FlowInputStreamPublisher ofInputStream(InputStream inputStream) {
        return new FlowInputStreamPublisher(inputStream);
    }

    public static FlowInputStreamPublisher ofInputStream(InputStream inputStream, int bufSize) {
        return new FlowInputStreamPublisher(inputStream, () -> IoUtils.X.newReadByteBuffer(bufSize));
    }

    private FlowInputStreamPublisher(InputStream inputStream) {
        this(inputStream, IoUtils.X::newReadByteBuffer);
    }

    private FlowInputStreamPublisher(InputStream inputStream, Supplier<ByteBuffer> supplier) {
        this.inputStream = inputStream;
        this.supplier = supplier;
    }

    @Override
    public void subscribe(Subscriber<? super List<ByteBuffer>> subscriber) {
        this.subscriber = subscriber;
        subscriber.onSubscribe(subscription);
    }

    private class FlowInputStreamSubscription implements Subscription {

        @Override
        public void request(long n) {
            if (!canceled && n > 0) {
                try {
                    ByteBuffer buf = supplier.get();
                    int readByte;
                    while (-1 != (readByte = inputStream.read())) {
                        buf.put((byte) readByte);
                        if (!buf.hasRemaining()) {
                            buf.flip();
                            subscriber.onNext(Collections.singletonList(buf));
                            buf = supplier.get();
                        }
                    }
                    if (buf.position() > 0) {
                        buf.flip();
                        subscriber.onNext(Collections.singletonList(buf));
                    }
                    subscriber.onComplete();
                } catch (IOException e) {
                    subscriber.onError(e);
                } finally {
                    IoUtils.X.closeQuietly(inputStream);
                }
            }
        }

        @Override
        public void cancel() {
            canceled = true;
        }
    }
}
