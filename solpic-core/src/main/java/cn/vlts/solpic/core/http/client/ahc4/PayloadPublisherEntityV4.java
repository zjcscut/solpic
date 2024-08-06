package cn.vlts.solpic.core.http.client.ahc4;

import cn.vlts.solpic.core.http.PayloadPublisher;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * PayloadPublisher entity.
 *
 * @author throwable
 * @since 2024/8/6 星期二 11:42
 */
public final class PayloadPublisherEntityV4 extends AbstractHttpEntity {

    private final PayloadPublisher payloadPublisher;

    private final long contentLength;

    public static PayloadPublisherEntityV4 newInstance(PayloadPublisher payloadPublisher,
                                                       long contentLength,
                                                       ContentType contentType) {
        return new PayloadPublisherEntityV4(payloadPublisher, contentLength, contentType, null);
    }

    public static PayloadPublisherEntityV4 newInstance(PayloadPublisher payloadPublisher,
                                                       ContentType contentType) {
        return new PayloadPublisherEntityV4(payloadPublisher, -1, contentType, null);
    }

    private PayloadPublisherEntityV4(PayloadPublisher payloadPublisher,
                                     long contentLength,
                                     ContentType contentType,
                                     String contentEncoding) {
        this.payloadPublisher = payloadPublisher;
        this.contentLength = contentLength;
        Optional.ofNullable(contentEncoding).ifPresent(this::setContentEncoding);
        Optional.ofNullable(contentType).ifPresent(ct -> setContentType(ct.toString()));
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        payloadPublisher.writeTo(outStream, false);
    }
}
