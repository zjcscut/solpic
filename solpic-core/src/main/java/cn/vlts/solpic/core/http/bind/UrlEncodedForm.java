package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.util.IoUtils;
import cn.vlts.solpic.core.util.Pair;
import cn.vlts.solpic.core.util.UriBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * Url encoded form, use for contentType = 'application/x-www-form-urlencoded'.
 *
 * @author throwable
 * @since 2024/8/9 星期五 12:02
 */
public class UrlEncodedForm implements PayloadPublisher, FlowPayloadPublisher {

    private final Charset charset;

    private final ContentType contentType;

    private final long contentLength;

    private final List<Pair> pairs;

    private final int size;

    UrlEncodedForm(Charset charset, ContentType contentType, long contentLength, List<Pair> pairs) {
        this.charset = charset;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.pairs = pairs;
        this.size = pairs.size();
    }

    public void consume(Consumer<Pair> consumer) {
        for (int i = 0; i < size; i++) {
            Pair pair = pairs.get(i);
            consumer.accept(Pair.of(pair.name(), pair.value()));
        }
    }

    public int getSize() {
        return size;
    }

    public String getName(int index) {
        return index >= 0 && index < size ? pairs.get(index).name() : null;
    }

    public String getValue(int index) {
        return index >= 0 && index < size ? pairs.get(index).value() : null;
    }

    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
        byte[] content = format().getBytes(charset);
        Subscription nop = new UrlEncodedFormSubscription();
        subscriber.onSubscribe(nop);
        try {
            subscriber.onNext(ByteBuffer.wrap(content));
        }finally {
            subscriber.onComplete();
        }
    }

    @Override
    public void writeTo(OutputStream outputStream, boolean autoClose) throws IOException {
        byte[] content = format().getBytes(charset);
        try {
            outputStream.write(content);
        } finally {
            if (autoClose) {
                IoUtils.X.closeQuietly(outputStream);
            }
        }
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    private String format() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                buf.append(UriBuilder.QUERY_PARAM_SEPARATOR);
            }
            Pair pair = pairs.get(i);
            buf.append(pair.name()).append(UriBuilder.PARAM_VALUE_SEPARATOR).append(pair.value());
        }
        return buf.toString();
    }

    public static Builder newBuilder() {
        return newBuilder(StandardCharsets.UTF_8);
    }

    public static Builder newBuilder(Charset charset) {
        return new UrlEncodedFormBuilder(charset);
    }

    public interface Builder {

        Builder add(String name, String value);

        Builder addEncoded(String name, String value);

        UrlEncodedForm build();
    }

    private static class UrlEncodedFormSubscription implements Subscription {

        @Override
        public void request(long n) {
            // no-op
        }

        @Override
        public void cancel() {
            // no-op
        }
    }
}
