package cn.vlts.solpic.core.http.client.ok;

import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.util.IoUtils;
import cn.vlts.solpic.core.util.ReflectionUtils;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;

import java.io.IOException;
import java.net.Proxy;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client base on OKHTTP 4.x.
 *
 * @author throwable
 * @since 2024/7/24 00:30
 */
@SuppressWarnings("unchecked")
public class OkHttpClientImpl extends BaseHttpClient implements HttpClient {

    private int connectTimeout = -1;

    private int readTimeout = -1;

    private int writeTimeout = -1;

    private OkHttpClient realHttpClient;

    public OkHttpClientImpl() {
        super();
        init();
    }

    private void init() {
        // support HTTP/1.0, HTTP/1.1, HTTP/2.0
        addHttpVersions(HttpVersion.HTTP_1, HttpVersion.HTTP_1_1, HttpVersion.HTTP_2);
        int connectTimeoutToUse = getConnectTimeout();
        int readTimeoutToUse = getReadTimeout();
        int writeTimeoutToUse = getWriteTimeout();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connectTimeoutToUse, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeoutToUse, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeoutToUse, TimeUnit.MILLISECONDS);
        Proxy proxyToUse = getProxy();
        if (Objects.nonNull(proxyToUse)) {
            builder.proxy(proxyToUse);
        }
        this.realHttpClient = builder.build();
    }

    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               RequestPayloadSupport payloadPublisher,
                                               ResponsePayloadSupport<?> payloadSubscriber) throws IOException {
        ResponsePayloadSupport<T> responsePayloadSupport = (ResponsePayloadSupport<T>) payloadSubscriber;
        String method = request.getMethod().name();
        boolean requiresRequestBody = HttpMethod.requiresRequestBody(method);
        // todo process request
        RequestBody requestBody = null;
        Request.Builder requestBuilder = new Request.Builder().url(request.getUri().toURL());
        requestBuilder.method(method, requestBody);
        request.consumeHeaders(httpHeader -> requestBuilder.addHeader(httpHeader.name(), httpHeader.value()));
        Request okHttpRequest = requestBuilder.build();
        // todo process response
        Response okHttpResponse = realHttpClient.newCall(okHttpRequest).execute();
        return null;
    }

    @Override
    protected void closeInternal() {
        Optional.ofNullable(realHttpClient).ifPresent(ohc -> {
            Cache cache = ohc.cache();
            if (Objects.nonNull(cache)) {
                IoUtils.X.closeQuietly(cache);
            }
            ohc.dispatcher().executorService().shutdown();
            ohc.connectionPool().evictAll();
        });
    }

    @Override
    public void setProxy(Proxy proxy) {
        super.setProxy(proxy);
        this.realHttpClient = this.realHttpClient.newBuilder().proxy(proxy).build();
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        this.realHttpClient = this.realHttpClient.newBuilder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    public int getConnectTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECT_TIMEOUT))
                .orElse(this.connectTimeout);
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        this.realHttpClient = this.realHttpClient.newBuilder()
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    public int getReadTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_READ_TIMEOUT))
                .orElse(this.readTimeout);
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
        this.realHttpClient = this.realHttpClient.newBuilder()
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    public int getWriteTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_WRITE_TIMEOUT))
                .orElse(this.writeTimeout);
    }

    static {
        ReflectionUtils.X.forName("okhttp3.OkHttpClient");
    }
}
