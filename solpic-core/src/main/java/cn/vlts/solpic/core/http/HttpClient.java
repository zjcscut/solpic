package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;
import cn.vlts.solpic.core.spi.DisposableBean;
import cn.vlts.solpic.core.spi.InitialingBean;
import cn.vlts.solpic.core.spi.Spi;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client.
 *
 * @author throwable
 * @since 2024/7/23 星期二 17:59
 */
@Spi(value = HttpClient.DEFAULT, singleton = false)
public interface HttpClient extends HttpOptional, MetricsSupport, Closeable, InitialingBean, DisposableBean {

    String DEFAULT = "default";

    String id();

    String spec();

    <T> HttpResponse<T> send(HttpRequest request,
                             ResponsePayloadSupport<?> payloadSubscriber);

    <T> T sendSimple(HttpRequest request,
                     ResponsePayloadSupport<?> payloadSubscriber);

    <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                     ResponsePayloadSupport<?> payloadSubscriber);

    <T> CompletableFuture<T> sendAsyncSimple(HttpRequest request,
                                             ResponsePayloadSupport<?> payloadSubscriber);

    @SuppressWarnings("rawtypes")
    <T> ListenableFuture<HttpResponse<T>> enqueue(HttpRequest request,
                                                  ResponsePayloadSupport<?> payloadSubscriber,
                                                  FutureListener... listeners);

    @SuppressWarnings("rawtypes")
    <T> ListenableFuture<T> enqueueSimple(HttpRequest request,
                                          ResponsePayloadSupport<?> payloadSubscriber,
                                          FutureListener... listeners);

    <T> ScheduledFuture<HttpResponse<T>> scheduledSend(HttpRequest request,
                                                       ResponsePayloadSupport<?> payloadSubscriber,
                                                       long delay,
                                                       TimeUnit unit,
                                                       CompletableFuture<HttpResponse<T>> promise);

    <T> ScheduledFuture<T> scheduledSendSimple(HttpRequest request,
                                               ResponsePayloadSupport<?> payloadSubscriber,
                                               long delay,
                                               TimeUnit unit,
                                               CompletableFuture<T> promise);

    boolean isRunning();

    @Override
    default void close() throws IOException {

    }

    @Override
    default void init() {

    }

    @Override
    default void destroy() throws Exception {

    }
}
