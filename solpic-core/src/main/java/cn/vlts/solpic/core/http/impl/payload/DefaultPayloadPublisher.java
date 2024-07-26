package cn.vlts.solpic.core.http.impl.payload;

import cn.vlts.solpic.core.common.PayloadSupportType;
import cn.vlts.solpic.core.http.PayloadPublisher;

/**
 * Default payload publisher.
 *
 * @author throwable
 * @since 2024/7/26 星期五 10:10
 */
public class DefaultPayloadPublisher implements PayloadPublisher {

    private final byte[] content;

    private long contentLength;

    public DefaultPayloadPublisher(byte[] content) {
        this(content, content.length);
    }

    public DefaultPayloadPublisher(byte[] content, long contentLength) {
        this.content = content;
        this.contentLength = contentLength;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public PayloadSupportType getType() {
        return PayloadSupportType.DEFAULT;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public byte[] content() {
        return this.content;
    }
}
