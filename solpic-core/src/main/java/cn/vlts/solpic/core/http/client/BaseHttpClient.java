package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;
import cn.vlts.solpic.core.concurrent.ThreadPool;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.exception.SolpicHttpException;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.HttpOptionSupport;
import cn.vlts.solpic.core.http.impl.ReadOnlyHttpRequest;
import cn.vlts.solpic.core.http.impl.ReadOnlyHttpResponse;
import cn.vlts.solpic.core.http.interceptor.HttpInterceptor;
import cn.vlts.solpic.core.spi.SpiLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Base HTTP client.
 *
 * @author throwable
 * @since 2024/7/24 星期三 11:24
 */
public abstract class BaseHttpClient extends HttpOptionSupport implements HttpOptional, HttpClient {

    private volatile ThreadPool threadPool;

    private final List<HttpInterceptor> interceptors = new ArrayList<>();

    public BaseHttpClient() {
        baseInit();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request,
                                    PayloadPublisher payloadPublisher,
                                    PayloadSubscriber<T> payloadSubscriber) {
        triggerInterceptorsBeforeSend(request);
        HttpResponse<T> response = null;
        try {
            response = sendInternal(request, payloadPublisher, payloadSubscriber);
            triggerInterceptorsAfterSend(request, response);
        } catch (IOException e) {
            throw new SolpicHttpException("Send HTTP request failed", e);
        } finally {
            triggerInterceptorsAfterCompletion(request, response);
        }
        return response;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            PayloadPublisher payloadPublisher,
                                                            PayloadSubscriber<T> payloadSubscriber) {
        return CompletableFuture.supplyAsync(() -> send(request, payloadPublisher, payloadSubscriber), getThreadPool());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> ListenableFuture<HttpResponse<T>> enqueue(HttpRequest request,
                                                         PayloadPublisher payloadPublisher,
                                                         PayloadSubscriber<T> payloadSubscriber,
                                                         FutureListener... listeners) {
        return getThreadPool().submit(() -> send(request, payloadPublisher, payloadSubscriber), listeners);
    }

    protected ThreadPool getThreadPool() {
        if (Objects.isNull(this.threadPool)) {
            synchronized (this) {
                if (Objects.isNull(this.threadPool)) {
                    String threadPoolName = ThreadPool.DEFAULT;
                    if (supportHttpOption(HttpOptions.HTTP_THREAD_POOL)) {
                        String threadPoolOption = getHttpOptionValue(HttpOptions.HTTP_THREAD_POOL);
                        if (Objects.nonNull(threadPoolOption)) {
                            threadPoolName = threadPoolOption;
                        }
                    }
                    this.threadPool = SpiLoader.getSpiLoader(ThreadPool.class).getService(threadPoolName);
                }
            }
        }
        return this.threadPool;
    }

    public void addInterceptor(HttpInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public void removeInterceptor(HttpInterceptor interceptor) {
        this.interceptors.removeIf(i -> i == interceptor);
    }

    private void triggerInterceptorsBeforeSend(HttpRequest request) {
        ReadOnlyHttpRequest readOnlyHttpRequest = ReadOnlyHttpRequest.of(request);
        this.interceptors.forEach(interceptor -> interceptor.beforeSend(readOnlyHttpRequest));
    }

    private void triggerInterceptorsAfterSend(HttpRequest request, HttpResponse<?> response) {
        ReadOnlyHttpRequest readOnlyHttpRequest = ReadOnlyHttpRequest.of(request);
        ReadOnlyHttpResponse<?> readOnlyHttpResponse = ReadOnlyHttpResponse.of(response);
        this.interceptors.forEach(interceptor -> interceptor.afterSend(readOnlyHttpRequest, readOnlyHttpResponse));
    }

    private void triggerInterceptorsAfterCompletion(HttpRequest request, HttpResponse<?> response) {
        ReadOnlyHttpRequest readOnlyHttpRequest = ReadOnlyHttpRequest.of(request);
        ReadOnlyHttpResponse<?> readOnlyHttpResponse = ReadOnlyHttpResponse.of(response);
        this.interceptors.forEach(interceptor -> interceptor.afterCompletion(readOnlyHttpRequest, readOnlyHttpResponse));
    }

    protected void baseInit() {
        List<HttpInterceptor> interceptorList = SpiLoader.getSpiLoader(HttpInterceptor.class).getAvailableServices();
        if (Objects.nonNull(interceptorList)) {
            this.interceptors.addAll(interceptorList);
        }
    }

    protected abstract <T> HttpResponse<T> sendInternal(HttpRequest request,
                                                        PayloadPublisher payloadPublisher,
                                                        PayloadSubscriber<T> payloadSubscriber) throws IOException;
}
