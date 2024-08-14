package cn.vlts.solpic.core.http.client.ahc5;

import cn.vlts.solpic.core.http.flow.FlowOutputStreamSubscriber;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * FlowPayloadPublisher entity.
 *
 * @author throwable
 * @since 2024/8/6 星期二 11:52
 */
public final class FlowPayloadPublisherEntityV5 extends AbstractHttpEntity {

    private final FlowPayloadPublisher flowPayloadPublisher;

    private final long contentLength;

    public static FlowPayloadPublisherEntityV5 newInstance(FlowPayloadPublisher flowPayloadPublisher,
                                                           long contentLength,
                                                           ContentType contentType) {
        return new FlowPayloadPublisherEntityV5(flowPayloadPublisher, contentLength, contentType, null);
    }

    public static FlowPayloadPublisherEntityV5 newInstance(FlowPayloadPublisher flowPayloadPublisher,
                                                           ContentType contentType) {
        return new FlowPayloadPublisherEntityV5(flowPayloadPublisher, -1, contentType,
                null);
    }

    private FlowPayloadPublisherEntityV5(FlowPayloadPublisher flowPayloadPublisher,
                                         long contentLength,
                                         ContentType contentType,
                                         String contentEncoding) {
        super(contentType, contentEncoding);
        this.flowPayloadPublisher = flowPayloadPublisher;
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
        flowPayloadPublisher.subscribe(FlowOutputStreamSubscriber.ofOutputStream(outStream));
    }
}
