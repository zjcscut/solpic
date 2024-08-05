package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client.
 *
 * @author throwable
 * @since 2024/7/23 星期二 17:59
 */
public interface HttpClient extends Closeable {

    String id();

    <T> HttpResponse<T> send(HttpRequest request,
                             RequestPayloadSupport payloadPublisher,
                             ResponsePayloadSupport<?> payloadSubscriber);

    <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                     RequestPayloadSupport payloadPublisher,
                                                     ResponsePayloadSupport<?> payloadSubscriber);

    @SuppressWarnings("rawtypes")
    <T> ListenableFuture<HttpResponse<T>> enqueue(HttpRequest request,
                                                  RequestPayloadSupport payloadPublisher,
                                                  ResponsePayloadSupport<?> payloadSubscriber,
                                                  FutureListener... listeners);

    <T> ScheduledFuture<HttpResponse<T>> scheduledSend(HttpRequest request,
                                                       RequestPayloadSupport payloadPublisher,
                                                       ResponsePayloadSupport<?> payloadSubscriber,
                                                       long delay,
                                                       TimeUnit unit,
                                                       CompletableFuture<HttpResponse<T>> promise);

    boolean isRunning();

    @Override
    default void close() throws IOException {

    }
}
