package cn.vlts.solpic.core.http;

import java.util.concurrent.CompletableFuture;

/**
 * HTTP client.
 *
 * @author throwable
 * @since 2024/7/23 星期二 17:59
 */
public interface HttpClient {

    default <T> HttpResponse<T> send(HttpRequest request) {
        return null;
    }

    default <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request) {
        return null;
    }
}
