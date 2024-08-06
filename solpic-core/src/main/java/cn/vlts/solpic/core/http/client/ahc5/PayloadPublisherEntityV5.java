package cn.vlts.solpic.core.http.client.ahc5;

import cn.vlts.solpic.core.http.PayloadPublisher;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * PayloadPublisher entity.
 *
 * @author throwable
 * @since 2024/8/6 星期二 11:42
 */
public final class PayloadPublisherEntityV5 extends AbstractHttpEntity {

    private final PayloadPublisher payloadPublisher;

    private final long contentLength;

    public static PayloadPublisherEntityV5 newInstance(PayloadPublisher payloadPublisher,
                                                       long contentLength,
                                                       ContentType contentType) {
        return new PayloadPublisherEntityV5(payloadPublisher, contentLength, contentType, null);
    }

    public static PayloadPublisherEntityV5 newInstance(PayloadPublisher payloadPublisher,
                                                       ContentType contentType) {
        return new PayloadPublisherEntityV5(payloadPublisher, -1, contentType, null);
    }

    private PayloadPublisherEntityV5(PayloadPublisher payloadPublisher,
                                     long contentLength,
                                     ContentType contentType,
                                     String contentEncoding) {
        super(contentType, contentEncoding);
        this.payloadPublisher = payloadPublisher;
        this.contentLength = contentLength;
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
    public void close() throws IOException {

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
