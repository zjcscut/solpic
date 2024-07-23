package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.common.HttpStatusCode;

/**
 * HTTP response.
 *
 * @author throwable
 * @since 2024/7/23 23:37
 */
public interface HttpResponse extends HttpMessage {

    HttpStatusCode getStatusCode();

    String getReasonPhrase();

    default boolean supportPayloadSubscriber() {
        return true;
    }

    <T> HttpPayloadSubscriber<T> getPayloadSubscriber();
}
