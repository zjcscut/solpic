package cn.vlts.solpic.core.http;

import java.io.InputStream;

/**
 * Payload subscriber.
 *
 * @author throwable
 * @since 2024/7/26 01:07
 */
public interface PayloadSubscriber<T> extends ResponsePayloadSupport<T> {

    void readFrom(InputStream inputStream);
}
