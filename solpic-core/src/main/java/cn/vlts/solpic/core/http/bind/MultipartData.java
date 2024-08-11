package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.http.flow.ByteBufferConsumerOutputStream;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.util.IoUtils;
import cn.vlts.solpic.core.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

/**
 * Multipart data.
 *
 * @author throwable
 * @since 2024/8/9 星期五 12:03
 */
public class MultipartData implements PayloadPublisher, FlowPayloadPublisher {

    private final ContentType contentType;

    private final long contentLength;

    private final List<Part> parts;

    MultipartData(ContentType contentType, long contentLength, List<Part> parts) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.parts = parts;
    }

    @Override
    public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
        Subscription subscription = new MultipartDataSubscription(subscriber);
        subscriber.onSubscribe(subscription);
    }

    @Override
    public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
        try {
            for (Part part : parts) {
                part.writeTo(outputStream);
            }
        } finally {
            if (autoClose) {
                IoUtils.X.closeQuietly(outputStream);
            }
        }
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    public static Builder newBuilder() {
        return new MultipartDataBuilder();
    }

    public static Builder newBuilder(String boundary, Charset charset) {
        return new MultipartDataBuilder(boundary, charset);
    }

    public interface Part {

        String getName();

        String getFileName();

        ContentType getContentType();

        long getContentLength();

        void addHeader(String name, String value);

        void removeHeader(String name);

        List<Pair> getAllHeaders();

        void writeTo(OutputStream outputStream) throws IOException;
    }

    public interface Builder {

        Builder addTextPart(String name, String value);

        Builder addTextPart(String name, String value, ContentType contentType);

        Builder addBinaryPart(String name, byte[] value);

        Builder addBinaryPart(String name, byte[] value, ContentType contentType);

        Builder addBinaryPart(String name, InputStream value);

        Builder addBinaryPart(String name, InputStream value, long contentLength, ContentType contentType);

        Builder addFilePart(String name, Path path);

        Builder addFilePart(String name, Path path, ContentType contentType);

        Builder addFilePart(String name, String filename, Path path, ContentType contentType);

        MultipartData build();
    }

    private class MultipartDataSubscription implements Subscription {

        private final Subscriber<? super ByteBuffer> subscriber;

        private final ByteBufferConsumerOutputStream outputStream;

        public MultipartDataSubscription(Subscriber<? super ByteBuffer> subscriber) {
            this.subscriber = subscriber;
            this.outputStream = new ByteBufferConsumerOutputStream(subscriber::onNext);
        }

        @Override
        public void request(long n) {
            if (n > 0) {
                boolean success = true;
                try {
                    for (Part part : parts) {
                        part.writeTo(outputStream);
                    }
                } catch (Throwable throwable) {
                    success = false;
                    subscriber.onError(throwable);
                } finally {
                    IoUtils.X.closeQuietly(outputStream);
                    if (success) {
                        subscriber.onComplete();
                    }
                }
            }
        }

        @Override
        public void cancel() {

        }
    }
}
