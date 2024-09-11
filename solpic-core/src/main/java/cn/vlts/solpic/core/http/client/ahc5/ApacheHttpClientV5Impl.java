package cn.vlts.solpic.core.http.client.ahc5;

import cn.vlts.solpic.core.common.UriScheme;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.config.SSLConfig;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.HttpVersion;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.http.flow.FlowInputStreamPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.impl.DefaultHttpResponse;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.util.IoUtils;
import cn.vlts.solpic.core.util.ReflectionUtils;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.TimeValue;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client base on Apache HTTP client 5.x.
 *
 * @author throwable
 * @since 2024/7/24 00:29
 */
@SuppressWarnings("unchecked")
public class ApacheHttpClientV5Impl extends BaseHttpClient implements HttpClient {

    private int connectTimeout = -1;

    private int socketTimeout = -1;

    private int connectionRequestTimeout = -1;

    private int responseTimeout = -1;

    private int connectionKeepAlive = -1;

    private int connectionMaxTotal = -1;

    private int connectionTtl = -1;

    private int connectionIdleTime = -1;

    private boolean evictExpiredConnections = true;

    private HttpClientConnectionManager connectionManager;

    private volatile CloseableHttpClient realHttpClient;

    public ApacheHttpClientV5Impl() {
        super();
    }

    @Override
    protected void initInternal() {
        // support HTTP/0.9, HTTP/1.0, HTTP/1.1, HTTP/2.0
        addHttpVersions(HttpVersion.HTTP_0_9, HttpVersion.HTTP_1, HttpVersion.HTTP_1_1, HttpVersion.HTTP_2);
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
                HttpOptions.HTTP_SOCKET_TIMEOUT,
                HttpOptions.HTTP_CONNECTION_REQUEST_TIMEOUT,
                HttpOptions.HTTP_RESPONSE_TIMEOUT
        );
    }

    public void rebuildRealClient() {
        // default connection config
        ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();
        int connectTimeoutToUse = getConnectTimeout();
        if (connectTimeoutToUse > 0) {
            connectionConfigBuilder.setConnectTimeout(connectTimeoutToUse, TimeUnit.MILLISECONDS);
        }
        int socketTimeoutToUse = getSocketTimeout();
        if (socketTimeoutToUse > 0) {
            connectionConfigBuilder.setSocketTimeout(socketTimeoutToUse, TimeUnit.MILLISECONDS);
        }
        // default request config
        RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
        int connectionRequestTimeoutToUse = getConnectionRequestTimeout();
        if (connectionRequestTimeoutToUse > 0) {
            defaultRequestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeoutToUse, TimeUnit.MILLISECONDS);
        }
        int responseTimeoutToUse = getResponseTimeout();
        if (responseTimeoutToUse > 0) {
            defaultRequestConfigBuilder.setResponseTimeout(responseTimeoutToUse, TimeUnit.MILLISECONDS);
        }
        int connectionKeepAliveToUse = getConnectionKeepAlive();
        if (connectionKeepAliveToUse > 0) {
            defaultRequestConfigBuilder.setConnectionKeepAlive(TimeValue.of(connectionKeepAliveToUse, TimeUnit.MILLISECONDS));
        }
        // connection pool
        if (Objects.equals(Boolean.TRUE, getHttpOptionValue(HttpOptions.HTTP_CLIENT_ENABLE_CONNECTION_POOL))) {
            RegistryBuilder<ConnectionSocketFactory> socketFactoryRegistryBuilder = RegistryBuilder.create();
            socketFactoryRegistryBuilder.register(UriScheme.HTTP.getValue(), new PlainConnectionSocketFactory());
            SSLConnectionSocketFactory sslConnectionSocketFactory = createSSLConnectionSocketFactory();
            socketFactoryRegistryBuilder.register(UriScheme.HTTPS.getValue(), sslConnectionSocketFactory);
            PoolingHttpClientConnectionManager connectionManager
                    = new PoolingHttpClientConnectionManager(socketFactoryRegistryBuilder.build());
            int connectionMaxTotalToUse = getConnectionMaxTotal();
            if (connectionMaxTotalToUse > 0) {
                connectionManager.setMaxTotal(connectionMaxTotalToUse);
            }
            int connectionTtlToUse = getConnectionTtl();
            if (connectionTtlToUse > 0) {
                connectionConfigBuilder.setTimeToLive(connectionTtlToUse, TimeUnit.MILLISECONDS);
            }
            connectionManager.setDefaultConnectionConfig(connectionConfigBuilder.build());
            this.connectionManager = connectionManager;
        }
        // proxy
        HttpHost proxyToUse = Optional.ofNullable(getProxy()).map(Proxy::address)
                .map(addr -> (InetSocketAddress) addr)
                .map(addr -> new HttpHost(addr.getHostName(), addr.getPort()))
                .orElse(null);
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setDefaultRequestConfig(defaultRequestConfigBuilder.build())
                .setConnectionManager(connectionManager)
                .setProxy(proxyToUse);
        if (isEvictExpiredConnections()) {
            httpClientBuilder.evictExpiredConnections();
        }
        int connectionIdleTimeToUse = getConnectionIdleTime();
        if (connectionIdleTimeToUse > 0) {
            httpClientBuilder.evictIdleConnections(TimeValue.of(connectionIdleTimeToUse, TimeUnit.MILLISECONDS));
        }
        this.realHttpClient = httpClientBuilder.build();
    }

    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               RequestPayloadSupport payloadPublisher,
                                               ResponsePayloadSupport<?> payloadSubscriber) throws IOException {
        ResponsePayloadSupport<T> responsePayloadSupport = (ResponsePayloadSupport<T>) payloadSubscriber;
        ClassicHttpRequest classicHttpRequest = createClassicHttpRequest(request, payloadPublisher);
        return getRealHttpClient().execute(classicHttpRequest, classicHttpResponse -> {
            try {
                return parseFromClassicHttpResponse(request, classicHttpResponse, responsePayloadSupport);
            } finally {
                IoUtils.X.closeQuietly(classicHttpResponse);
            }
        });
    }

    private ClassicHttpRequest createClassicHttpRequest(HttpRequest request,
                                                        RequestPayloadSupport payloadPublisher) throws IOException {
        HttpMethod method = request.getMethod();
        HttpUriRequestBase base = new HttpUriRequestBase(method.name(), request.getUri());
        Integer connectionRequestTimeoutToUse
                = request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_CONNECTION_REQUEST_TIMEOUT);
        Integer responseTimeoutToUse = request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_RESPONSE_TIMEOUT);
        Integer connectionKeepAliveToUse = request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_CONNECTION_KEEPALIVE);
        if (Objects.nonNull(connectionRequestTimeoutToUse) || Objects.nonNull(responseTimeoutToUse) ||
                Objects.nonNull(connectionKeepAliveToUse)) {
            RequestConfig.Builder builder = RequestConfig.custom();
            Optional.ofNullable(connectionRequestTimeoutToUse)
                    .ifPresent(v -> builder.setConnectionRequestTimeout(v, TimeUnit.MILLISECONDS));
            Optional.ofNullable(responseTimeoutToUse)
                    .ifPresent(v -> builder.setResponseTimeout(v, TimeUnit.MILLISECONDS));
            Optional.ofNullable(connectionKeepAliveToUse)
                    .ifPresent(v -> builder.setConnectionKeepAlive(TimeValue.of(connectionKeepAliveToUse,
                            TimeUnit.MILLISECONDS)));
            base.setConfig(builder.build());
        }
        request.consumeHeaders(httpHeader -> base.addHeader(httpHeader.name(), httpHeader.value()));
        String contentTypeValue = request.getContentTypeValue();
        org.apache.hc.core5.http.ContentType contentType = null;
        if (Objects.nonNull(contentTypeValue)) {
            contentType = org.apache.hc.core5.http.ContentType.parse(contentTypeValue);
        }
        if (request.supportPayload() || isForceWriteRequestPayload(request)) {
            long contentLength = request.getContentLength();
            if (contentLength <= 0) {
                contentLength = payloadPublisher.contentLength();
            }
            if (payloadPublisher instanceof PayloadPublisher) {
                PayloadPublisher publisher = (PayloadPublisher) payloadPublisher;
                if (contentLength <= 0) {
                    base.setEntity(PayloadPublisherEntityV5.newInstance(publisher, contentType));
                } else {
                    base.setEntity(PayloadPublisherEntityV5.newInstance(publisher, contentLength, contentType));
                }
            } else if (payloadPublisher instanceof FlowPayloadPublisher) {
                FlowPayloadPublisher flowPublisher = (FlowPayloadPublisher) payloadPublisher;
                if (contentLength <= 0) {
                    base.setEntity(FlowPayloadPublisherEntityV5.newInstance(flowPublisher, contentType));
                } else {
                    base.setEntity(FlowPayloadPublisherEntityV5.newInstance(flowPublisher, contentLength, contentType));
                }
            }
        }
        return base;
    }

    private <T> HttpResponse<T> parseFromClassicHttpResponse(HttpRequest request,
                                                             ClassicHttpResponse classicHttpResponse,
                                                             ResponsePayloadSupport<T> responsePayloadSupport) throws IOException {
        HttpEntity responseEntity = classicHttpResponse.getEntity();
        if (Objects.nonNull(responseEntity) && Objects.nonNull(responseEntity.getContent())) {
            if (responsePayloadSupport instanceof PayloadSubscriber) {
                PayloadSubscriber<T> subscriber = (PayloadSubscriber<T>) responsePayloadSupport;
                subscriber.readFrom(responseEntity.getContent());
            } else if (responsePayloadSupport instanceof FlowPayloadSubscriber) {
                FlowPayloadSubscriber<T> flowSubscriber = (FlowPayloadSubscriber<T>) responsePayloadSupport;
                FlowInputStreamPublisher.ofInputStream(responseEntity.getContent()).subscribe(flowSubscriber);
            }
        } else {
            // force to discard
            responsePayloadSupport = PayloadSubscribers.X.discarding();
        }
        DefaultHttpResponse<T> httpResponse = new DefaultHttpResponse<>(responsePayloadSupport.getPayload(),
                classicHttpResponse.getCode());
        httpResponse.setReasonPhrase(classicHttpResponse.getReasonPhrase());
        HttpVersion httpVersion = HttpVersion.defaultVersion();
        ProtocolVersion protocolVersion = classicHttpResponse.getVersion();
        HttpVersion parsedHttpVersion = HttpVersion.fromVersion(protocolVersion.getMajor(), protocolVersion.getMinor());
        if (Objects.nonNull(parsedHttpVersion)) {
            httpVersion = parsedHttpVersion;
        }
        httpResponse.setProtocolVersion(httpVersion);
        Header[] responseHeaders = classicHttpResponse.getHeaders();
        if (Objects.nonNull(responseHeaders)) {
            for (Header responseHeader : responseHeaders) {
                httpResponse.addHeader(responseHeader.getName(), responseHeader.getValue());
            }
        }
        httpResponse.setHttpClient(this);
        httpResponse.setHttpRequest(request);
        return httpResponse;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECT_TIMEOUT))
                .orElse(this.connectTimeout);
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getSocketTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_SOCKET_TIMEOUT))
                .orElse(this.socketTimeout);
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

    public int getConnectionRequestTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECTION_REQUEST_TIMEOUT))
                .orElse(this.connectionRequestTimeout);
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getResponseTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_RESPONSE_TIMEOUT))
                .orElse(this.responseTimeout);
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public int getConnectionKeepAlive() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECTION_KEEPALIVE))
                .orElse(this.connectionKeepAlive);
    }

    public void setConnectionKeepAlive(int connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
    }

    public int getConnectionIdleTime() {
        return connectionIdleTime;
    }

    public void setConnectionIdleTime(int connectionIdleTime) {
        this.connectionIdleTime = connectionIdleTime;
    }

    public boolean isEvictExpiredConnections() {
        return evictExpiredConnections;
    }

    public void setEvictExpiredConnections(boolean evictExpiredConnections) {
        this.evictExpiredConnections = evictExpiredConnections;
    }

    public void setConnectionManager(HttpClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public CloseableHttpClient getRealHttpClient() {
        if (Objects.isNull(this.realHttpClient)) {
            synchronized (this) {
                if (Objects.isNull(this.realHttpClient)) {
                    rebuildRealClient();
                }
            }
        }
        return this.realHttpClient;
    }

    @Override
    protected void closeInternal() throws IOException {
        Optional.ofNullable(realHttpClient).ifPresent(ahc -> {
            Optional.ofNullable(connectionManager).ifPresent(IoUtils.X::closeQuietly);
            IoUtils.X.closeQuietly(ahc);
        });
    }

    private SSLConnectionSocketFactory createSSLConnectionSocketFactory() {
        SSLConfig sslConfig = getHttpOptionValue(HttpOptions.HTTP_SSL_CONFIG);
        if (Objects.nonNull(sslConfig) && Objects.nonNull(sslConfig.getContext())) {
            HostnameVerifier hostnameVerifier = sslConfig.getHostnameVerifier();
            if (Objects.isNull(hostnameVerifier)) {
                hostnameVerifier = new NoopHostnameVerifier();
            }
            return new SSLConnectionSocketFactory(sslConfig.getContext(), hostnameVerifier);
        }
        return SSLConnectionSocketFactory.getSocketFactory();
    }

    static {
        ReflectionUtils.X.forName("org.apache.hc.client5.http.impl.classic.CloseableHttpClient");
    }
}
