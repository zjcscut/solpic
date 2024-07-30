package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Payload publisher impls.
 *
 * @author throwable
 * @since 2024/7/26 星期五 10:45
 */
public enum PayloadPublishers {
    X;

    private static final ConcurrentMap<Type, Function<?, PayloadPublisher>> BUILD_IN_CACHE = new ConcurrentHashMap<>();

    public static final DefaultPayloadPublishers DEFAULT;

    public static final StreamPayloadPublishers STREAM;

    public <T> Function<T, PayloadPublisher> getBuildInPayloadPublisher(Type type) {
        return Objects.nonNull(type) ? (Function<T, PayloadPublisher>) BUILD_IN_CACHE.get(type) : null;
    }

    public boolean containsBuildInPayloadPublisher(Type type) {
        return BUILD_IN_CACHE.containsKey(type);
    }

    private static class DiscardingPayloadPublisher implements PayloadPublisher {

        @Override
        public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
            if (autoClose) {
                IoUtils.X.closeQuietly(outputStream);
            }
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    private static class ByteArrayPayloadPublisher implements PayloadPublisher {

        private final byte[] bytes;

        private final int offset;

        private final int length;

        public ByteArrayPayloadPublisher(byte[] bytes) {
            this(bytes, 0, bytes.length);
        }

        public ByteArrayPayloadPublisher(byte[] bytes, int offset, int length) {
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
            try {
                outputStream.write(bytes, offset, length);
            } finally {
                if (autoClose) {
                    IoUtils.X.closeQuietly(outputStream);
                }
            }
        }

        @Override
        public long getContentLength() {
            return length;
        }
    }

    private static class InputStreamPayloadPublisher implements PayloadPublisher {

        private final Supplier<? extends InputStream> supplier;

        public InputStreamPayloadPublisher(Supplier<? extends InputStream> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
            try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(supplier.get(),
                    StandardCharsets.UTF_8))) {
                int b;
                while (-1 != (b = reader.read())) {
                    outputStream.write(b);
                }
            } finally {
                if (autoClose) {
                    IoUtils.X.closeQuietly(outputStream);
                }
            }
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    public static class DefaultPayloadPublishers {

        public PayloadPublisher discarding() {
            return new DiscardingPayloadPublisher();
        }

        public PayloadPublisher ofString(String content) {
            return ofString(content, StandardCharsets.UTF_8);
        }

        public PayloadPublisher ofString(String content, Charset charset) {
            return ofByteArray(content.getBytes(charset));
        }

        public PayloadPublisher ofByteArray(byte[] bytes) {
            return ofByteArray(bytes, 0, bytes.length);
        }

        public PayloadPublisher ofByteArray(byte[] bytes, int offset, int length) {
            int sl = bytes.length;
            if (offset == 0 && sl == length) {
                return new ByteArrayPayloadPublisher(bytes);
            }
            return new ByteArrayPayloadPublisher(bytes, offset, length);
        }

        public PayloadPublisher ofInputStream(InputStream in) {
            return new InputStreamPayloadPublisher(() -> in);
        }

        public PayloadPublisher ofFile(Path path) {
            InputStream inputStream;
            try {
                inputStream = Files.newInputStream(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new InputStreamPayloadPublisher(() -> inputStream);
        }
    }

    public static class StreamPayloadPublishers {

    }

    static {
        DEFAULT = new DefaultPayloadPublishers();
        BUILD_IN_CACHE.put(String.class, (String s) -> DEFAULT.ofString(s));
        BUILD_IN_CACHE.put(byte[].class, (byte[] bytes) -> DEFAULT.ofByteArray(bytes));
        BUILD_IN_CACHE.put(InputStream.class, (InputStream in) -> DEFAULT.ofInputStream(in));
        BUILD_IN_CACHE.put(Path.class, (Path path) -> DEFAULT.ofFile(path));
        STREAM = new StreamPayloadPublishers();
    }
}
