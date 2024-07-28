package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.common.HttpStatus;
import cn.vlts.solpic.core.common.HttpStatusCode;

/**
 * HTTP response.
 *
 * @author throwable
 * @since 2024/7/23 23:37
 */
public interface HttpResponse<T> extends HttpMessage {

    HttpStatusCode getStatusCode();

    default String getReasonPhrase() {
        HttpStatusCode statusCode = getStatusCode();
        if (statusCode instanceof HttpStatus) {
            return ((HttpStatus) statusCode).reasonPhrase();
        }
        return null;
    }

    default boolean supportPayload() {
        return true;
    }

    T getPayload();

    HttpRequest getHttpRequest();

    HttpClient getHttpClient();
}
