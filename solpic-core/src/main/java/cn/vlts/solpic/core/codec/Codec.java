package cn.vlts.solpic.core.codec;

import cn.vlts.solpic.core.flow.MinimalFuture;
import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.http.PayloadSubscriber;
import cn.vlts.solpic.core.util.IoUtils;
import com.alibaba.fastjson.JSON;

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
            byte[] content = IoUtils.X.copyByteBuffersToByteArray(bufferList);
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

            private long contentLength = 0;

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
            public long getContentLength() {
                return this.contentLength;
            }
        };
    }

    default PayloadSubscriber<T> createPayloadSubscriber(Type targetType) {
        return new PayloadSubscriber<T>() {

            private final AtomicBoolean read = new AtomicBoolean();

            private CompletionStage<T> future;

            @Override
            public void readFrom(InputStream inputStream) {
                if (this.read.compareAndSet(false, true)) {
                    try {
                        if (Objects.nonNull(inputStream)) {
                            T result = read(inputStream, targetType);
                            this.future = MinimalFuture.completedFuture(result);
                        } else {
                            this.future = MinimalFuture.completedFuture(null);
                        }
                    } catch (IOException e) {
                        this.future = MinimalFuture.failedFuture(e);
                    }
                }
            }

            @Override
            public CompletionStage<T> getPayload() {
                return this.future;
            }

            @Override
            public long getContentLength() {
                return -1;
            }
        };
    }
}
