package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.flow.Publisher;

import java.nio.ByteBuffer;

/**
 * HTTP payload publisher.
 *
 * @author throwable
 * @since 2024/7/23 23:54
 */
public interface HttpPayloadPublisher extends Publisher<ByteBuffer> {

    long getContentLength();
}
