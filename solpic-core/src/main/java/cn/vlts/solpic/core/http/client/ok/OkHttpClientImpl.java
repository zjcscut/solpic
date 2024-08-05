package cn.vlts.solpic.core.http.client.ok;

import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.http.flow.FlowInputStreamPublisher;
import cn.vlts.solpic.core.http.flow.FlowOutputStreamSubscriber;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.impl.DefaultHttpResponse;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.util.IoUtils;
import cn.vlts.solpic.core.util.ReflectionUtils;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;
import okio.BufferedSink;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private ConnectionPool connectionPool;

    private OkHttpClient realHttpClient;

    public OkHttpClientImpl() {
        super();
        init();
    }

    private void init() {
        // support HTTP/1.0, HTTP/1.1, HTTP/2.0
        addHttpVersions(HttpVersion.HTTP_1, HttpVersion.HTTP_1_1, HttpVersion.HTTP_2);
        // minimum options and available options
        addAvailableHttpOptions(
                HttpOptions.HTTP_CLIENT_ID,
                HttpOptions.HTTP_PROXY,
                HttpOptions.HTTP_ENABLE_LOGGING,
                HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE,
                HttpOptions.HTTP_ENABLE_EXECUTE_TRACING,
                HttpOptions.HTTP_FORCE_WRITE,
                HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS,
                HttpOptions.HTTP_CONNECT_TIMEOUT,
                HttpOptions.HTTP_READ_TIMEOUT,
                HttpOptions.HTTP_WRITE_TIMEOUT,
                HttpOptions.HTTP_CLIENT_ENABLE_CONNECTION_POOL,
                HttpOptions.HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE,
                HttpOptions.HTTP_CLIENT_CONNECTION_POOL_TTL
        );
        int connectTimeoutToUse = getConnectTimeout();
        int readTimeoutToUse = getReadTimeout();
        int writeTimeoutToUse = getWriteTimeout();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (connectTimeoutToUse >= 0) {
            builder.connectTimeout(connectTimeoutToUse, TimeUnit.MILLISECONDS);
        }
        if (readTimeoutToUse >= 0) {
            builder.readTimeout(readTimeoutToUse, TimeUnit.MILLISECONDS);
        }
        if (writeTimeoutToUse >= 0) {
            builder.writeTimeout(writeTimeoutToUse, TimeUnit.MILLISECONDS);
        }
        Proxy proxyToUse = getProxy();
        if (Objects.nonNull(proxyToUse)) {
            builder.proxy(proxyToUse);
        }
        ConnectionPool connectionPoolToUse = getConnectionPool();
        if (Objects.nonNull(connectionPoolToUse)) {
            builder.connectionPool(connectionPoolToUse);
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
        String contentTypeValue = request.getContentTypeValue();
        MediaType mediaType = Objects.nonNull(contentTypeValue) ? MediaType.parse(contentTypeValue) : null;
        RequestBody requestBody = null;
        if (request.supportPayload() || supportHttpOption(HttpOptions.HTTP_FORCE_WRITE)) {
            requestBody = new OkHttpRequestBody(mediaType, request.getContentLength(), payloadPublisher);
        } else if (requiresRequestBody) {
            requestBody = RequestBody.create(new byte[0], mediaType);
        }
        Request.Builder requestBuilder = new Request.Builder().url(request.getUri().toURL());
        requestBuilder.method(method, requestBody);
        request.consumeHeaders(httpHeader -> requestBuilder.addHeader(httpHeader.name(), httpHeader.value()));
        Request okHttpRequest = requestBuilder.build();
        Response okHttpResponse = realHttpClient.newCall(okHttpRequest).execute();
        ResponseBody responseBody = okHttpResponse.body();
        if (Objects.nonNull(responseBody)) {
            if (responsePayloadSupport instanceof PayloadSubscriber) {
                PayloadSubscriber<T> subscriber = (PayloadSubscriber<T>) responsePayloadSupport;
                subscriber.readFrom(responseBody.byteStream());
            } else if (responsePayloadSupport instanceof FlowPayloadSubscriber) {
                FlowPayloadSubscriber<T> flowSubscriber = (FlowPayloadSubscriber<T>) responsePayloadSupport;
                FlowInputStreamPublisher.ofInputStream(responseBody.byteStream()).subscribe(flowSubscriber);
            }
        } else {
            // force to discard
            responsePayloadSupport = PayloadSubscribers.X.discarding();
        }
        DefaultHttpResponse<T> httpResponse = new DefaultHttpResponse<>(responsePayloadSupport.getPayload(),
                okHttpResponse.code());
        Headers responseHeaders = okHttpResponse.headers();
        for (String headerName : responseHeaders.names()) {
            List<String> headerValues = responseHeaders.values(headerName);
            for (String headerValue : headerValues) {
                httpResponse.addHeader(headerName, headerValue);
            }
        }
        return httpResponse;
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

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.realHttpClient = this.realHttpClient.newBuilder()
                .connectionPool(connectionPool)
                .build();
    }

    public ConnectionPool getConnectionPool() {
        Integer max = getHttpOptionValue(HttpOptions.HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE);
        Integer ttl = getHttpOptionValue(HttpOptions.HTTP_CLIENT_CONNECTION_POOL_TTL);
        if (supportHttpOption(HttpOptions.HTTP_CLIENT_ENABLE_CONNECTION_POOL) &&
                Objects.nonNull(max) && Objects.nonNull(ttl)) {
            return new ConnectionPool(max, ttl, TimeUnit.MILLISECONDS);
        }
        return this.connectionPool;
    }

    private static class OkHttpRequestBody extends RequestBody {

        private final RequestPayloadSupport payloadPublisher;

        private final long contentLength;

        private final MediaType mediaType;

        public OkHttpRequestBody(MediaType mediaType,
                                 long contentLength,
                                 RequestPayloadSupport payloadPublisher) {
            this.mediaType = mediaType;
            this.contentLength = contentLength;
            this.payloadPublisher = payloadPublisher;
        }

        @Override
        public long contentLength() throws IOException {
            return contentLength;
        }

        @Override
        public MediaType contentType() {
            return mediaType;
        }

        @Override
        public void writeTo(BufferedSink bufferedSink) throws IOException {
            if (payloadPublisher instanceof PayloadPublisher) {
                ((PayloadPublisher) payloadPublisher).writeTo(bufferedSink.outputStream());
            } else if (payloadPublisher instanceof FlowPayloadPublisher) {
                FlowPayloadPublisher flowPublisher = (FlowPayloadPublisher) payloadPublisher;
                flowPublisher.subscribe(FlowOutputStreamSubscriber.ofOutputStream(bufferedSink.outputStream()));
            }
        }
    }

    @Override
    protected void closeInternal() {
        Optional.ofNullable(realHttpClient).ifPresent(ohc -> {
            Optional.ofNullable(ohc.cache()).ifPresent(IoUtils.X::closeQuietly);
            ohc.dispatcher().executorService().shutdown();
            Optional.ofNullable(ohc.connectionPool()).ifPresent(ConnectionPool::evictAll);
        });
    }

    static {
        ReflectionUtils.X.forName("okhttp3.OkHttpClient");
    }
}
