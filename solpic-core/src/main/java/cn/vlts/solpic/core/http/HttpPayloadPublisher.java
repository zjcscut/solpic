package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.flow.Publisher;

/**
 * HTTP payload publisher.
 *
 * @author throwable
 * @since 2024/7/23 23:54
 */
public interface HttpPayloadPublisher extends Publisher<byte[]> {

    long getContentLength();
}
