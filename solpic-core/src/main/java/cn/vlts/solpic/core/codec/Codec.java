package cn.vlts.solpic.core.codec;

import cn.vlts.solpic.core.flow.MinimalFuture;
import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.http.PayloadSubscriber;
import cn.vlts.solpic.core.http.flow.ByteBufferConsumerOutputStream;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.flow.PullingInputStream;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Codec.
 *
 * @author throwable
 * @since 2024/7/26 星期五 14:36
 */
public interface Codec<S, T> {

    byte[] toByteArray(S s);

    default ByteBuffer toByteBuffer(S s) {
        return ByteBuffer.wrap(toByteArray(s));
    }

    default List<ByteBuffer> toByteBuffers(S s) {
        byte[] byteArray = toByteArray(s);
        return IoUtils.X.copyByteArrayToByteBuffers(byteArray);
    }

    T fromByteArray(byte[] bytes, Type targetType);

    default T fromByteBuffer(ByteBuffer buffer, Type targetType) {
        if (buffer.hasRemaining()) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return fromByteArray(bytes, targetType);
        }
        return null;
    }

    default T fromByteBuffers(List<ByteBuffer> bufferList, Type targetType) {
        if (Objects.nonNull(bufferList)) {
            byte[] content = IoUtils.X.fastCopyByteBuffersToByteArray(bufferList);
            if (content.length > 0) {
                return fromByteArray(content, targetType);
            }
        }
        return null;
    }

    int write(OutputStream outputStream, S s) throws IOException;

    T read(InputStream inputStream, Type targetType) throws IOException;

    default PayloadPublisher createPayloadPublisher(S s) {
        return new PayloadPublisher() {

            private final AtomicBoolean written = new AtomicBoolean();

            private long contentLength = -1;

            @Override
            public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
                if (written.compareAndSet(false, true)) {
                    try {
                        this.contentLength = write(outputStream, s);
                    } finally {
                        if (autoClose) {
                            IoUtils.X.closeQuietly(outputStream);
                        }
                    }
                }
            }

            @Override
            public long contentLength() {
                return this.contentLength;
            }
        };
    }

    default PayloadSubscriber<T> createPayloadSubscriber(Type targetType) {
        return new PayloadSubscriber<T>() {

            private final AtomicBoolean read = new AtomicBoolean();

            private final CompletableFuture<T> future = new MinimalFuture<>();

            @Override
            public void readFrom(InputStream inputStream, boolean autoClose) {
                if (this.read.compareAndSet(false, true)) {
                    try {
                        T result = read(inputStream, targetType);
                        future.complete(result);
                    } catch (IOException e) {
                        future.completeExceptionally(e);
                    } finally {
                        if (autoClose) {
                            IoUtils.X.closeQuietly(inputStream);
                        }
                    }
                }
            }

            @Override
            public CompletionStage<T> getPayload() {
                return this.future;
            }
        };
    }

    default FlowPayloadPublisher createFlowPayloadPublisher(S s) {

        class PayloadPublisherSubscription implements Subscription {

            private final ByteBufferConsumerOutputStream bcos;

            private final S payload;

            private final Subscriber<? super ByteBuffer> subscriber;

            private final AtomicBoolean published = new AtomicBoolean();

            private volatile boolean canceled;

            public PayloadPublisherSubscription(S payload,
                                                Subscriber<? super ByteBuffer> subscriber) {
                this.payload = payload;
                this.bcos = new ByteBufferConsumerOutputStream(subscriber::onNext);
                this.subscriber = subscriber;
            }

            @Override
            public void request(long n) {
                if (!canceled && published.compareAndSet(false, true)) {
                    try {
                        write(bcos, payload);
                    } catch (IOException e) {
                        subscriber.onError(e);
                    } finally {
                        IoUtils.X.closeQuietly(bcos);
                    }
                    subscriber.onComplete();
                }
            }

            @Override
            public void cancel() {
                canceled = true;
            }
        }

        return new FlowPayloadPublisher() {

            @Override
            public long contentLength() {
                return -1;
            }

            @Override
            public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
                PayloadPublisherSubscription subscription = new PayloadPublisherSubscription(s, subscriber);
                subscriber.onSubscribe(subscription);
            }
        };
    }

    default FlowPayloadSubscriber<T> createFlowPayloadSubscriber(Type targetType) {

        return new FlowPayloadSubscriber<T>() {

            private final AtomicBoolean subscribed = new AtomicBoolean();

            private final CompletableFuture<T> result = new MinimalFuture<>();

            private volatile Subscription subscription;

            private volatile PullingInputStream pullingInputStream;

            @Override
            public CompletionStage<T> getPayload() {
                return result;
            }

            @Override
            public void onSubscribe(Subscription subscription) {
                if (subscribed.compareAndSet(false, true)) {
                    this.subscription = subscription;
                    pullingInputStream = new PullingInputStream(subscription);
                    subscription.request(1);
                } else {
                    subscription.cancel();
                }
            }

            @Override
            public void onNext(List<ByteBuffer> item) {
                for (ByteBuffer buffer : item) {
                    if (buffer.hasRemaining()) {
                        try {
                            pullingInputStream.offer(buffer);
                        } catch (IOException e) {
                            result.completeExceptionally(e);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                result.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                try {
                    result.complete(read(pullingInputStream, targetType));
                } catch (Throwable throwable) {
                    result.completeExceptionally(throwable);
                } finally {
                    IoUtils.X.closeQuietly(pullingInputStream);
                }
            }
        };
    }
}
