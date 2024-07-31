package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.flow.MinimalFuture;
import cn.vlts.solpic.core.http.PayloadSubscriber;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Payload subscriber impls.
 *
 * @author throwable
 * @since 2024/7/28 22:29
 */
public enum PayloadSubscribers {
    X;

    private static final ConcurrentMap<Type, PayloadSubscriber<?>> BUILD_IN_CACHE = new ConcurrentHashMap<>();

    public static final DefaultPayloadSubscribers DEFAULT;

    @SuppressWarnings("unchecked")
    public <T> PayloadSubscriber<T> getBuildInPayloadSubscriber(Type type) {
        return Objects.nonNull(type) ? (PayloadSubscriber<T>) BUILD_IN_CACHE.get(type) : DEFAULT.discarding();
    }

    public boolean containsBuildInPayloadSubscriber(Type type) {
        return BUILD_IN_CACHE.containsKey(type);
    }

    private static class ByteArrayPayloadSubscriber implements PayloadSubscriber<byte[]> {

        private long contentLength;

        private CompletionStage<byte[]> future;

        @Override
        public void readFrom(InputStream inputStream) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(IoUtils.READ_BUF_SIZE);
            try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(inputStream))) {
                int b;
                while (-1 != (b = reader.read())) {
                    bos.write(b);
                }
                future = MinimalFuture.completedFuture(bos.toByteArray());
            } catch (IOException e) {
                future = MinimalFuture.failedFuture(e);
            }
        }

        @Override
        public CompletionStage<byte[]> getPayload() {
            return future;
        }

        @Override
        public long getContentLength() {
            return contentLength;
        }
    }

    private static class StringPayloadSubscriber implements PayloadSubscriber<String> {

        private final Charset charset;

        public StringPayloadSubscriber() {
            this(StandardCharsets.UTF_8);
        }

        public StringPayloadSubscriber(Charset charset) {
            this.charset = charset;
        }

        private long contentLength;

        private CompletionStage<String> future;

        @Override
        public void readFrom(InputStream inputStream) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(IoUtils.READ_BUF_SIZE);
            try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(inputStream,
                    StandardCharsets.UTF_8))) {
                int b;
                while (-1 != (b = reader.read())) {
                    bos.write(b);
                }
                future = MinimalFuture.completedFuture(new String(bos.toByteArray(), charset));
            } catch (IOException e) {
                future = MinimalFuture.failedFuture(e);
            }
        }

        @Override
        public CompletionStage<String> getPayload() {
            return future;
        }

        @Override
        public long getContentLength() {
            return contentLength;
        }
    }

    private static class DiscardingPayloadSubscriber<T> implements PayloadSubscriber<T> {

        @Override
        public void readFrom(InputStream inputStream) {

        }

        @Override
        public CompletionStage<T> getPayload() {
            return null;
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    private static class FilePayloadSubscriber implements PayloadSubscriber<Void> {

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
        public void readFrom(InputStream inputStream) {
            try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(inputStream, charset));
                 BufferedWriter bufferedWriter = Files.newBufferedWriter(targetPath, charset)) {
                int b;
                while (-1 != (b = reader.read())) {
                    bufferedWriter.write(b);
                }
            } catch (IOException e) {
                result.completeExceptionally(e);
            }
        }

        @Override
        public CompletionStage<Void> getPayload() {
            return result;
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    public static class DefaultPayloadSubscribers {

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
    }

    static {
        DEFAULT = new DefaultPayloadSubscribers();
        BUILD_IN_CACHE.put(Void.class, DEFAULT.discarding());
        BUILD_IN_CACHE.put(String.class, DEFAULT.ofString());
        BUILD_IN_CACHE.put(byte[].class, DEFAULT.ofByteArray());
    }
}
