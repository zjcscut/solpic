package cn.vlts.solpic.core.http.client.jhc;

import cn.vlts.solpic.core.concurrent.ThreadPool;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.config.SSLConfig;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublishers;
import cn.vlts.solpic.core.http.impl.DefaultHttpResponse;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.http.HttpHeaders;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * JDK11 HTTP client, base on java.net.http.HttpClient.
 *
 * @author throwable
 * @since 2024/8/6 星期二 16:37
 */
@SuppressWarnings("unchecked")
public class JdkHttpClientImpl extends BaseHttpClient implements HttpClient, HttpOptional {

    private static final String CONNECTION_POOL_CAPACITY_PROPERTY_KEY = "jdk.httpclient.connectionPoolSize";

    private static final String CONNECTION_KEEPALIVE_TIMEOUT_KEY = "jdk.httpclient.keepalive.timeout";

    private int connectTimeout = -1;

    private int connectionMaxTotal = -1;

    private int connectionTtl = -1;

    private int requestTimeout = -1;

    private java.net.http.HttpClient realHttpClient;

    public JdkHttpClientImpl() {
        super();
    }

    @Override
    protected void initInternal() {
        // support HTTP/1.1, HTTP/2.0
        addHttpVersions(HttpVersion.HTTP_1_1, HttpVersion.HTTP_2);
        // minimum options and available options
        addAvailableHttpOptions(
                // common options -- start
                HttpOptions.HTTP_CLIENT_ID,
                HttpOptions.HTTP_THREAD_POOL,
                HttpOptions.HTTP_SCHEDULED_THREAD_POOL,
                HttpOptions.HTTP_PROTOCOL_VERSION,
                HttpOptions.HTTP_SSL_CONFIG,
                HttpOptions.HTTP_PROXY,
                HttpOptions.HTTP_ENABLE_LOGGING,
                HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE,
                HttpOptions.HTTP_ENABLE_EXECUTE_TRACING,
                HttpOptions.HTTP_FORCE_WRITE,
                HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS,
                HttpOptions.HTTP_CLIENT_METRICS,
                // common options -- end
                // connection pool options -- start
                HttpOptions.HTTP_CLIENT_ENABLE_CONNECTION_POOL,
                HttpOptions.HTTP_CLIENT_CONNECTION_POOL_CAPACITY,
                HttpOptions.HTTP_CLIENT_CONNECTION_TTL,
                // connection pool options -- end
                HttpOptions.HTTP_CONNECT_TIMEOUT,
                HttpOptions.HTTP_TIMEOUT
        );
        // build real client
        rebuildRealClient();
    }

    public void rebuildRealClient() {
        String capturedConnectionPoolCapacityVal = System.getProperty(CONNECTION_POOL_CAPACITY_PROPERTY_KEY);
        String capturedConnectionKeepaliveVal = System.getProperty(CONNECTION_KEEPALIVE_TIMEOUT_KEY);
        boolean modifyConnectionPoolCapacity = false;
        boolean modifyConnectionKeepalive = false;
        if (Objects.equals(Boolean.TRUE, supportHttpOption(HttpOptions.HTTP_CLIENT_ENABLE_CONNECTION_POOL))) {
            int connectionMaxTotalToUse = getConnectionMaxTotal();
            int connectionTtlToUse = getConnectionTtl();
            if (connectionMaxTotalToUse >= 0) {
                System.setProperty(CONNECTION_POOL_CAPACITY_PROPERTY_KEY, String.valueOf(connectionMaxTotalToUse));
                modifyConnectionPoolCapacity = true;
            }
            connectionTtlToUse = connectionTtlToUse / 1000;
            if (connectionTtlToUse > 0) {
                System.setProperty(CONNECTION_KEEPALIVE_TIMEOUT_KEY, String.valueOf(connectionTtlToUse));
                modifyConnectionKeepalive = true;
            }
        }
        try {
            java.net.http.HttpClient.Builder httpClientBuilder = java.net.http.HttpClient.newBuilder();
            int connectTimeoutToUse = getConnectTimeout();
            if (connectTimeoutToUse > 0) {
                httpClientBuilder.connectTimeout(Duration.ofMillis(connectTimeoutToUse));
            }
            SSLConfig sslConfigToUse = getHttpOptionValue(HttpOptions.HTTP_SSL_CONFIG);
            if (Objects.nonNull(sslConfigToUse) && !Objects.equals(sslConfigToUse, SSLConfig.NO)) {
                Optional.ofNullable(sslConfigToUse.getContext()).ifPresent(httpClientBuilder::sslContext);
                Optional.ofNullable(sslConfigToUse.getParams()).ifPresent(httpClientBuilder::sslParameters);
            }
            Proxy proxyToUse = getProxy();
            if (Objects.nonNull(proxyToUse)) {
                httpClientBuilder.proxy(ProxySelector.of((InetSocketAddress) proxyToUse.address()));
            }
            HttpVersion httpVersionToUse = getHttpOptionValue(HttpOptions.HTTP_PROTOCOL_VERSION);
            if (Objects.nonNull(httpVersionToUse)) {
                if (Objects.equals(HttpVersion.HTTP_1_1, httpVersionToUse)) {
                    httpClientBuilder.version(java.net.http.HttpClient.Version.HTTP_1_1);
                } else if (Objects.equals(HttpVersion.HTTP_2, httpVersionToUse)) {
                    httpClientBuilder.version(java.net.http.HttpClient.Version.HTTP_2);
                }
            }
            ThreadPool executor = getThreadPool();
            httpClientBuilder.executor(executor);
            realHttpClient = httpClientBuilder.build();
        } finally {
            if (modifyConnectionPoolCapacity) {
                System.setProperty(CONNECTION_POOL_CAPACITY_PROPERTY_KEY, capturedConnectionPoolCapacityVal);
            }
            if (modifyConnectionKeepalive) {
                System.setProperty(CONNECTION_KEEPALIVE_TIMEOUT_KEY, capturedConnectionKeepaliveVal);
            }
        }
    }

    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               RequestPayloadSupport payloadPublisher,
                                               ResponsePayloadSupport<?> payloadSubscriber)
            throws IOException, InterruptedException {
        ResponsePayloadSupport<T> responsePayloadSupport = (ResponsePayloadSupport<T>) payloadSubscriber;
        java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder();
        requestBuilder.uri(request.getUri());
        int requestTimeoutToUse = getRequestTimeout();
        if (requestTimeoutToUse > 0) {
            requestBuilder.timeout(Duration.ofMillis(requestTimeoutToUse));
        }
        if (request.supportPayload() || isForceWriteRequestPayload()) {
            BodyPublisherAdapter bodyPublisherAdapter = BodyPublisherAdapter.newInstance(payloadPublisher);
            requestBuilder.method(request.getRawMethod(), bodyPublisherAdapter);
        } else {
            BodyPublisherAdapter bodyPublisherAdapter =
                    BodyPublisherAdapter.newInstance(FlowPayloadPublishers.X.discarding());
            requestBuilder.method(request.getRawMethod(), bodyPublisherAdapter);
        }
        request.consumeHeaders(httpHeader -> requestBuilder.header(httpHeader.name(), httpHeader.value()));
        java.net.http.HttpRequest httpRequest = requestBuilder.build();
        BodyHandlerAdapter<T> bodyHandlerAdapter = BodyHandlerAdapter.newInstance(responsePayloadSupport);
        java.net.http.HttpResponse<T> httpResponse = realHttpClient.send(httpRequest, bodyHandlerAdapter);
        DefaultHttpResponse<T> response = new DefaultHttpResponse<>(responsePayloadSupport.getPayload(),
                httpResponse.statusCode());
        java.net.http.HttpClient.Version version = httpResponse.version();
        HttpVersion httpVersion = HttpVersion.defaultVersion();
        if (Objects.equals(java.net.http.HttpClient.Version.HTTP_1_1, version)) {
            httpVersion = HttpVersion.HTTP_1_1;
        } else if (Objects.equals(java.net.http.HttpClient.Version.HTTP_2, version)) {
            httpVersion = HttpVersion.HTTP_2;
        }
        response.setProtocolVersion(httpVersion);
        HttpHeaders responseHeaders = httpResponse.headers();
        if (Objects.nonNull(responseHeaders)) {
            Map<String, List<String>> responseHeaderMap = responseHeaders.map();
            if (Objects.nonNull(responseHeaderMap)) {
                responseHeaderMap.forEach((k, vl) -> vl.forEach(v -> response.addHeader(k, v)));
            }
        }
        return response;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECT_TIMEOUT))
                .orElse(this.connectTimeout);
    }

    public void setConnectionMaxTotal(int connectionMaxTotal) {
        this.connectionMaxTotal = connectionMaxTotal;
    }

    public int getConnectionMaxTotal() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CLIENT_CONNECTION_POOL_CAPACITY))
                .orElse(this.connectionMaxTotal);
    }

    public void setConnectionTtl(int connectionTtl) {
        this.connectionTtl = connectionTtl;
    }

    public int getConnectionTtl() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CLIENT_CONNECTION_TTL))
                .orElse(this.connectionTtl);
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getRequestTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_REQUEST_TIMEOUT))
                .orElse(Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_TIMEOUT)).orElse(this.requestTimeout));
    }

    public java.net.http.HttpClient getRealHttpClient() {
        return this.realHttpClient;
    }

    static {
        ReflectionUtils.X.forName("java.net.http.HttpClient");
    }
}
