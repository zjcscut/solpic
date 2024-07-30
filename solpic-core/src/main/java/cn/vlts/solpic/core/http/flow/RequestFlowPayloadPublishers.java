package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.PullPublisher;
import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Request flow payload publisher.
 *
 * @author throwable
 * @since 2024/7/30 星期二 17:42
 */
public final class RequestFlowPayloadPublishers {

    private RequestFlowPayloadPublishers() {
        throw new Error();
    }

    public static class ByteArrayFlowPayloadPublisher implements FlowPayloadPublisher {

        private final int length;

        private final byte[] content;

        private final int offset;

        private final int bufSize;

        public ByteArrayFlowPayloadPublisher(byte[] content) {
            this(content, 0, content.length, IoUtils.READ_BUF_SIZE);
        }

        public ByteArrayFlowPayloadPublisher(byte[] content, int offset, int length) {
            this(content, 0, length, IoUtils.READ_BUF_SIZE);
        }

        public ByteArrayFlowPayloadPublisher(byte[] content, int offset, int length, int bufSize) {
            this.content = content;
            this.offset = offset;
            this.length = length;
            this.bufSize = bufSize;
        }

        @Override
        public long contentLength() {
            return length;
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            List<ByteBuffer> bufferList = copy(content, offset, length);
            new PullPublisher<>(bufferList).subscribe(subscriber);
        }

        List<ByteBuffer> copy(byte[] content, int offset, int length) {
            List<ByteBuffer> buffers = new ArrayList<>();
            while (length > 0) {
                ByteBuffer b = ByteBuffer.allocate(Math.min(bufSize, length));
                int max = b.capacity();
                int toCopy = Math.min(max, length);
                b.put(content, offset, toCopy);
                offset += toCopy;
                length -= toCopy;
                b.flip();
                buffers.add(b);
            }
            return buffers;
        }
    }

    public static class InputStreamPublisher implements FlowPayloadPublisher {

        private final Supplier<? extends InputStream> supplier;

        public InputStreamPublisher(Supplier<? extends InputStream> supplier) {
            this.supplier = supplier;
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            PullPublisher<ByteBuffer> publisher;
            InputStream inputStream = supplier.get();
            if (Objects.isNull(inputStream)) {
                publisher = new PullPublisher<>(new IllegalArgumentException("InputStream must not be null"));
            } else {
                publisher = new PullPublisher<>(new ArrayList<>());
            }
            publisher.subscribe(subscriber);
        }
    }
}
