package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client.
 *
 * @author throwable
 * @since 2024/7/23 星期二 17:59
 */
public interface HttpClient<R extends RequestPayloadSupport, U extends ResponsePayloadSupport> extends Closeable {


    default <T> HttpResponse<T> send(HttpRequest request, R payloadPublisher, U payloadSubscriber) {
        return null;
    }

    <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, R payloadPublisher, U payloadSubscriber);

    @SuppressWarnings("rawtypes")
    <T> ListenableFuture<HttpResponse<T>> enqueue(HttpRequest request,
                                                  R payloadPublisher,
                                                  U payloadSubscriber,
                                                  FutureListener... listeners);

    @Override
    default void close() throws IOException {

    }
}
