package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client.
 *
 * @author throwable
 * @since 2024/7/23 星期二 17:59
 */
public interface HttpClient extends Closeable {

    default <T> HttpResponse<T> send(HttpRequest request,
                                     FlowPayloadPublisher payloadPublisher,
                                     FlowPayloadSubscriber<T> payloadSubscriber) {
        return null;
    }

    <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                     FlowPayloadPublisher payloadPublisher,
                                                     FlowPayloadSubscriber<T> payloadSubscriber);

    @SuppressWarnings("rawtypes")
    <T> ListenableFuture<HttpResponse<T>> enqueue(HttpRequest request,
                                                  FlowPayloadPublisher payloadPublisher,
                                                  FlowPayloadSubscriber<T> payloadSubscriber,
                                                  FutureListener... listeners);

    @Override
    default void close() throws IOException {

    }
}
