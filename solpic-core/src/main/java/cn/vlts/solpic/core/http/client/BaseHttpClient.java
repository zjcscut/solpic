package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.common.HttpRequestStatus;
import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;
import cn.vlts.solpic.core.concurrent.ThreadPool;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.config.ProxyConfig;
import cn.vlts.solpic.core.exception.SolpicHttpException;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.HttpOptionSupport;
import cn.vlts.solpic.core.http.impl.ReadOnlyHttpRequest;
import cn.vlts.solpic.core.http.impl.ReadOnlyHttpResponse;
import cn.vlts.solpic.core.http.interceptor.HttpInterceptor;
import cn.vlts.solpic.core.spi.SpiLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base HTTP client.
 *
 * @author throwable
 * @since 2024/7/24 星期三 11:24
 */
public abstract class BaseHttpClient extends HttpOptionSupport implements HttpOptional, HttpClient {

    private final AtomicLong index = new AtomicLong();

    private final List<HttpInterceptor> interceptors = new ArrayList<>();

    private volatile ThreadPool threadPool;

    private String id;

    protected Proxy proxy;

    public BaseHttpClient() {
        baseInit();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request,
                                    RequestPayloadSupport payloadPublisher,
                                    ResponsePayloadSupport<?> payloadSubscriber) {
        triggerBeforeSend(request);
        HttpResponse<T> response = null;
        try {
            response = sendInternal(request, payloadPublisher, payloadSubscriber);
            triggerAfterSend(request, response);
        } catch (IOException e) {
            triggerOnError(request, e);
            throw new SolpicHttpException("Send HTTP request failed", e);
        } finally {
            triggerAfterCompletion(request, response);
        }
        return response;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            RequestPayloadSupport payloadPublisher,
                                                            ResponsePayloadSupport<?> payloadSubscriber) {
        return CompletableFuture.supplyAsync(() -> send(request, payloadPublisher, payloadSubscriber), getThreadPool());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> ListenableFuture<HttpResponse<T>> enqueue(HttpRequest request,
                                                         RequestPayloadSupport payloadPublisher,
                                                         ResponsePayloadSupport<?> payloadSubscriber,
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

    private void triggerInterceptorsOnError(HttpRequest request, Throwable throwable) {
        ReadOnlyHttpRequest readOnlyHttpRequest = ReadOnlyHttpRequest.of(request);
        this.interceptors.forEach(interceptor -> interceptor.onError(readOnlyHttpRequest, throwable));
    }

    private void triggerInterceptorsAfterCompletion(HttpRequest request, HttpResponse<?> response) {
        ReadOnlyHttpRequest readOnlyHttpRequest = ReadOnlyHttpRequest.of(request);
        ReadOnlyHttpResponse<?> readOnlyHttpResponse = ReadOnlyHttpResponse.of(response);
        this.interceptors.forEach(interceptor -> interceptor.afterCompletion(readOnlyHttpRequest, readOnlyHttpResponse));
    }

    protected void triggerBeforeSend(HttpRequest request) {
        changeRequestStatus(request, HttpRequestStatus.PROCESSING);
        triggerInterceptorsBeforeSend(request);
    }

    protected void triggerAfterSend(HttpRequest request, HttpResponse<?> response) {
        triggerInterceptorsAfterSend(request, response);
    }

    protected void triggerOnError(HttpRequest request, Throwable throwable) {
        changeRequestStatus(request, HttpRequestStatus.FAILED);
        triggerInterceptorsOnError(request, throwable);
    }

    protected void triggerAfterCompletion(HttpRequest request, HttpResponse<?> response) {
        triggerInterceptorsAfterCompletion(request, response);
        // copy request attachments to response
        if (supportHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS)) {
            response.copyAttachable(request);
        }
        // mark request finished
        changeRequestStatus(request, HttpRequestStatus.FINISHED);
    }

    private void changeRequestStatus(HttpRequest request, HttpRequestStatus status) {
        if (request instanceof DefaultHttpRequest) {
            ((DefaultHttpRequest) request).changeStatus(status);
        }
    }

    protected void baseInit() {
        List<HttpInterceptor> interceptorList = SpiLoader.getSpiLoader(HttpInterceptor.class).getAvailableServices();
        if (Objects.nonNull(interceptorList)) {
            this.interceptors.addAll(interceptorList);
        }
        // client id
        String clientId = getHttpOptionValue(HttpOptions.HTTP_CLIENT_ID);
        if (Objects.nonNull(clientId)) {
            this.id = clientId;
        } else {
            this.id = getClass().getSimpleName() + "-" + index.incrementAndGet();
        }
    }

    @Override
    public void close() throws IOException {
        closeInternal();
    }

    protected void closeInternal() {

    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public Proxy getProxy() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_PROXY))
                .filter(proxyConfig -> !Objects.equals(ProxyConfig.NO, proxyConfig))
                .map(proxyConfig -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getHostname(),
                        proxyConfig.getPort())))
                .orElse(this.proxy);
    }

    protected abstract <T> HttpResponse<T> sendInternal(HttpRequest request,
                                                        RequestPayloadSupport payloadPublisher,
                                                        ResponsePayloadSupport<?> payloadSubscriber) throws IOException;
}
