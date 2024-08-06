package cn.vlts.solpic.core.http.client.ahc4;

import cn.vlts.solpic.core.http.flow.FlowOutputStreamSubscriber;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * FlowPayloadPublisher entity.
 *
 * @author throwable
 * @since 2024/8/6 星期二 11:52
 */
public final class FlowPayloadPublisherEntityV4 extends AbstractHttpEntity {

    private final FlowPayloadPublisher flowPayloadPublisher;

    private final long contentLength;

    public static FlowPayloadPublisherEntityV4 newInstance(FlowPayloadPublisher flowPayloadPublisher,
                                                           long contentLength,
                                                           ContentType contentType) {
        return new FlowPayloadPublisherEntityV4(flowPayloadPublisher, contentLength, contentType, null);
    }

    public static FlowPayloadPublisherEntityV4 newInstance(FlowPayloadPublisher flowPayloadPublisher,
                                                           ContentType contentType) {
        return new FlowPayloadPublisherEntityV4(flowPayloadPublisher, -1, contentType, null);
    }

    private FlowPayloadPublisherEntityV4(FlowPayloadPublisher flowPayloadPublisher,
                                         long contentLength,
                                         ContentType contentType,
                                         String contentEncoding) {
        this.flowPayloadPublisher = flowPayloadPublisher;
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
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        flowPayloadPublisher.subscribe(FlowOutputStreamSubscriber.ofOutputStream(outStream));
    }
}
