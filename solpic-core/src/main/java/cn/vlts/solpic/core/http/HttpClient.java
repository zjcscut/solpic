package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

/**
 * HTTP client.
 *
 * @author throwable
 * @since 2024/7/23 星期二 17:59
 */
public interface HttpClient {

    default <T> HttpResponse<T> send(HttpRequest request,
                                     PayloadPublisher payloadPublisher,
                                     PayloadSubscriber<T> payloadSubscriber) {
        return null;
    }

    <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                     PayloadPublisher payloadPublisher,
                                                     PayloadSubscriber<T> payloadSubscriber);

    <T> ListenableFuture<HttpResponse<T>> enqueue(HttpRequest request,
                                                  PayloadPublisher payloadPublisher,
                                                  PayloadSubscriber<T> payloadSubscriber,
                                                  FutureListener... listeners);
}
