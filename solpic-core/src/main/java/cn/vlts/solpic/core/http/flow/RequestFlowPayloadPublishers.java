package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.Publisher;
import cn.vlts.solpic.core.flow.PullPublisher;
import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Request flow payload publishers.
 *
 * @author throwable
 * @since 2024/7/30 星期二 17:42
 */
public final class RequestFlowPayloadPublishers {

    private RequestFlowPayloadPublishers() {
        throw new Error();
    }

    public static class EmptyFlowPayloadPublisher implements FlowPayloadPublisher {

        private final Publisher<? extends ByteBuffer> publisher = new PullPublisher<>(Collections.emptyList());

        @Override
        public long contentLength() {
            return 0;
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            publisher.subscribe(subscriber);
        }
    }

    public static class IterableByteBufferFlowPayloadPublisher implements FlowPayloadPublisher {

        private final Iterable<? extends ByteBuffer> iterable;

        public IterableByteBufferFlowPayloadPublisher(Iterable<? extends ByteBuffer> iterable) {
            this.iterable = iterable;
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            new PullPublisher<>(iterable).subscribe(subscriber);
        }
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
            List<ByteBuffer> bufferList = IoUtils.X.copyByteArrayToByteBuffers(content, offset, length, bufSize);
            new PullPublisher<>(bufferList).subscribe(subscriber);
        }
    }

    public static class IterableByteArrayFlowPayloadPublisher implements FlowPayloadPublisher {

        private final Iterable<? extends byte[]> iterable;


        public IterableByteArrayFlowPayloadPublisher(Iterable<? extends byte[]> iterable) {
            this.iterable = iterable;
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            new PullPublisher<>(() -> new ByteArrayIterableIterator(iterable)).subscribe(subscriber);
        }
    }

    public static class StringFlowPayloadPublisher extends ByteArrayFlowPayloadPublisher {

        public StringFlowPayloadPublisher(String content, Charset charset) {
            super(content.getBytes(charset));
        }
    }

    public static class InputStreamFlowPayloadPublisher implements FlowPayloadPublisher {

        private final Supplier<? extends InputStream> supplier;

        public InputStreamFlowPayloadPublisher(Supplier<? extends InputStream> supplier) {
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
                publisher = new PullPublisher<>(() -> new InputStreamIterator(inputStream));
            }
            publisher.subscribe(subscriber);
        }
    }

    public static class FlowPayloadPublisherAdapter implements FlowPayloadPublisher {

        private final Publisher<? extends ByteBuffer> publisher;

        private final long contentLength;

        public FlowPayloadPublisherAdapter(Publisher<? extends ByteBuffer> publisher, long contentLength) {
            this.publisher = publisher;
            this.contentLength = contentLength;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            publisher.subscribe(subscriber);
        }
    }

    public static class FileFlowPayloadPublisher implements FlowPayloadPublisher {

        private final Path path;

        private final long length;

        public FileFlowPayloadPublisher(Path path) {
            this.path = Objects.requireNonNull(path);
            this.length = getFileLength();
            checkFileStatus();
        }

        @Override
        public long contentLength() {
            return length;
        }

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
            InputStream inputStream = null;
            Throwable error = null;
            try {
                inputStream = Files.newInputStream(path);
            } catch (IOException e) {
                error = e;
            }
            PullPublisher<ByteBuffer> publisher;
            InputStream in = inputStream;
            if (Objects.nonNull(error)) {
                publisher = new PullPublisher<>(error);
            } else {
                publisher = new PullPublisher<>(() -> new InputStreamIterator(in));
            }
            publisher.subscribe(subscriber);
        }

        private void checkFileStatus() {
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("File '" + path + "' not found");
            }
        }

        private long getFileLength() {
            try {
                return Files.size(path);
            } catch (IOException ignore) {

            }
            return -1;
        }
    }
}
