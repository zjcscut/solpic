package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.flow.MinimalFuture;
import cn.vlts.solpic.core.http.PayloadSubscriber;
import cn.vlts.solpic.core.util.ArgumentUtils;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Payload subscriber impls.
 *
 * @author throwable
 * @since 2024/7/28 22:29
 */
@SuppressWarnings("unchecked")
public enum PayloadSubscribers {
    X;

    private static final ConcurrentMap<Type, Supplier<PayloadSubscriber<?>>> CACHE = new ConcurrentHashMap<>();

    public <T> PayloadSubscriber<T> getPayloadSubscriber(Type type) {
        return Objects.isNull(type) ? discarding() : Optional.ofNullable(CACHE.get(type))
                .map(supplier -> (PayloadSubscriber<T>) supplier.get())
                .orElse(null);
    }

    public boolean containsPayloadSubscriber(Type type) {
        return CACHE.containsKey(type);
    }

    public static void registerPayloadSubscriber(Type type,
                                                 Supplier<PayloadSubscriber<?>> supplier) {
        ArgumentUtils.X.notNull("type", type);
        ArgumentUtils.X.notNull("supplier", supplier);
        CACHE.putIfAbsent(type, supplier);
    }

    public <T> PayloadSubscriber<T> discarding() {
        return new DiscardingPayloadSubscriber<>();
    }

    public PayloadSubscriber<String> ofString() {
        return new StringPayloadSubscriber();
    }

    public PayloadSubscriber<String> ofString(Charset charset) {
        return new StringPayloadSubscriber(charset);
    }

    public PayloadSubscriber<byte[]> ofByteArray() {
        return new ByteArrayPayloadSubscriber();
    }

    public PayloadSubscriber<Void> ofFile(Path path, Charset charset) {
        return new FilePayloadSubscriber(path, charset);
    }

    public PayloadSubscriber<Void> ofFile(Path path) {
        return new FilePayloadSubscriber(path);
    }

    private static class ByteArrayPayloadSubscriber implements PayloadSubscriber<byte[]> {

        private final AtomicBoolean read = new AtomicBoolean();

        private long contentLength;

        private final CompletableFuture<byte[]> result = new MinimalFuture<>();

        @Override
        public void readFrom(InputStream inputStream, boolean autoClose) {
            if (read.compareAndSet(false, true)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(IoUtils.READ_BUF_SIZE);
                try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(inputStream))) {
                    int b;
                    while (-1 != (b = reader.read())) {
                        bos.write(b);
                    }
                    result.complete(bos.toByteArray());
                } catch (IOException e) {
                    result.completeExceptionally(e);
                } finally {
                    if (autoClose) {
                        IoUtils.X.closeQuietly(inputStream);
                    }
                }
            }
        }

        @Override
        public CompletionStage<byte[]> getPayload() {
            return result;
        }
    }

    private static class StringPayloadSubscriber implements PayloadSubscriber<String> {

        private final AtomicBoolean read = new AtomicBoolean();

        private final Charset charset;

        private long contentLength;

        private final CompletableFuture<String> result = new MinimalFuture<>();

        public StringPayloadSubscriber() {
            this(StandardCharsets.UTF_8);
        }

        public StringPayloadSubscriber(Charset charset) {
            this.charset = charset;
        }

        @Override
        public void readFrom(InputStream inputStream, boolean autoClose) {
            if (read.compareAndSet(false, true)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(IoUtils.READ_BUF_SIZE);
                try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(inputStream,
                        StandardCharsets.UTF_8))) {
                    int b;
                    while (-1 != (b = reader.read())) {
                        bos.write(b);
                    }
                    result.complete(new String(bos.toByteArray(), charset));
                } catch (IOException e) {
                    result.completeExceptionally(e);
                } finally {
                    if (autoClose) {
                        IoUtils.X.closeQuietly(inputStream);
                    }
                }
            }
        }

        @Override
        public CompletionStage<String> getPayload() {
            return result;
        }
    }

    private static class DiscardingPayloadSubscriber<T> implements PayloadSubscriber<T> {

        private final CompletableFuture<T> cf = new MinimalFuture<>();

        @Override
        public void readFrom(InputStream inputStream, boolean autoClose) {
            cf.complete(null);
        }

        @Override
        public CompletionStage<T> getPayload() {
            return cf;
        }
    }

    private static class FilePayloadSubscriber implements PayloadSubscriber<Void> {

        private final AtomicBoolean read = new AtomicBoolean();

        private final CompletableFuture<Void> result = new MinimalFuture<>();

        private final Path targetPath;

        private final Charset charset;

        public FilePayloadSubscriber(Path targetPath) {
            this(targetPath, StandardCharsets.UTF_8);
        }

        public FilePayloadSubscriber(Path targetPath, Charset charset) {
            this.targetPath = targetPath;
            this.charset = charset;
        }

        @Override
        public void readFrom(InputStream inputStream, boolean autoClose) {
            if (read.compareAndSet(false, true)) {
                try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(inputStream, charset));
                     BufferedWriter bufferedWriter = Files.newBufferedWriter(targetPath, charset)) {
                    int b;
                    while (-1 != (b = reader.read())) {
                        bufferedWriter.write(b);
                    }
                } catch (IOException e) {
                    result.completeExceptionally(e);
                } finally {
                    if (autoClose) {
                        IoUtils.X.closeQuietly(inputStream);
                    }
                }
            }
        }

        @Override
        public CompletionStage<Void> getPayload() {
            return result;
        }
    }

    static {
        CACHE.put(Void.class, PayloadSubscribers.X::discarding);
        CACHE.put(void.class, PayloadSubscribers.X::discarding);
        CACHE.put(String.class, PayloadSubscribers.X::ofString);
        CACHE.put(byte[].class, PayloadSubscribers.X::ofByteArray);
    }
}
