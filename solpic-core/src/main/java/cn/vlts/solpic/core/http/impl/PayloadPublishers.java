package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.util.ArgumentUtils;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Payload publisher impls.
 *
 * @author throwable
 * @since 2024/7/26 星期五 10:45
 */
@SuppressWarnings("unchecked")
public enum PayloadPublishers {
    X;

    private static final ConcurrentMap<Type, Function<?, PayloadPublisher>> CACHE = new ConcurrentHashMap<>();

    public <T> Function<T, PayloadPublisher> getPayloadPublisher(Type type) {
        return Objects.isNull(type) ? s -> discarding() : Optional.ofNullable(CACHE.get(type))
                .map(function -> (Function<T, PayloadPublisher>) function)
                .orElse(null);
    }

    public boolean containsPayloadPublisher(Type type) {
        return CACHE.containsKey(type);
    }

    public void registerPayloadPublisher(Type type,
                                         Function<?, PayloadPublisher> function) {
        ArgumentUtils.X.notNull("type", type);
        ArgumentUtils.X.notNull("function", function);
        CACHE.putIfAbsent(type, function);
    }

    private static class DiscardingPayloadPublisher implements PayloadPublisher {

        private final AtomicBoolean written = new AtomicBoolean(false);

        @Override
        public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
            if (written.compareAndSet(false, true)) {
                if (autoClose) {
                    IoUtils.X.closeQuietly(outputStream);
                }
            }
        }

        @Override
        public long contentLength() {
            return 0;
        }
    }

    private static class ByteArrayPayloadPublisher implements PayloadPublisher {

        private final byte[] bytes;

        private final int offset;

        private final int length;

        private final AtomicBoolean written = new AtomicBoolean(false);

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
            if (written.compareAndSet(false, true)) {
                try {
                    outputStream.write(bytes, offset, length);
                } finally {
                    if (autoClose) {
                        IoUtils.X.closeQuietly(outputStream);
                    }
                }
            }
        }

        @Override
        public long contentLength() {
            return length;
        }
    }

    private static class InputStreamPayloadPublisher implements PayloadPublisher {

        private final Supplier<? extends InputStream> supplier;

        private final long length;

        private final AtomicBoolean written = new AtomicBoolean(false);

        public InputStreamPayloadPublisher(Supplier<? extends InputStream> supplier, long length) {
            this.supplier = supplier;
            this.length = length;
        }

        @Override
        public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
            if (written.compareAndSet(false, true)) {
                InputStream inputStream = supplier.get();
                try {
                    int b;
                    while (-1 != (b = inputStream.read())) {
                        outputStream.write(b);
                    }
                } finally {
                    if (autoClose) {
                        IoUtils.X.closeQuietly(outputStream);
                        IoUtils.X.closeQuietly(inputStream);
                    }
                }
            }
        }

        @Override
        public long contentLength() {
            return this.length;
        }
    }

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
        return new InputStreamPayloadPublisher(() -> in, -1);
    }

    public PayloadPublisher ofFile(Path path) {
        InputStream inputStream;
        long length;
        try {
            inputStream = Files.newInputStream(path);
            length = Files.size(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new InputStreamPayloadPublisher(() -> inputStream, length);
    }

    static {
        CACHE.put(String.class, (String s) -> PayloadPublishers.X.ofString(s));
        CACHE.put(Void.class, (Void v) -> PayloadPublishers.X.discarding());
        CACHE.put(byte[].class, (byte[] bytes) -> PayloadPublishers.X.ofByteArray(bytes));
        CACHE.put(InputStream.class, (InputStream in) -> PayloadPublishers.X.ofInputStream(in));
        CACHE.put(Path.class, (Path path) -> PayloadPublishers.X.ofFile(path));
    }
}
