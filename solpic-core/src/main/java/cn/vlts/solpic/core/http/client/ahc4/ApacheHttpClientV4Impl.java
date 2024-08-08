package cn.vlts.solpic.core.http.client.ahc4;

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
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client base on Apache HTTP client 4.x.
 *
 * @author throwable
 * @since 2024/7/24 00:29
 */
@SuppressWarnings("unchecked")
public class ApacheHttpClientV4Impl extends BaseHttpClient implements HttpClient {

    private int connectTimeout = -1;

    private int socketTimeout = -1;

    private int connectionRequestTimeout = -1;

    private int connectionMaxTotal = -1;

    private int connectionIdleTime = -1;

    private boolean evictExpiredConnections = true;

    private HttpClientConnectionManager connectionManager;

    private CloseableHttpClient realHttpClient;

    public ApacheHttpClientV4Impl() {
        super();
        init();
    }

    private void init() {
        // support HTTP/0.9, HTTP/1.0, HTTP/1.1, HTTP/2.0
        addHttpVersions(HttpVersion.HTTP_0_9, HttpVersion.HTTP_1, HttpVersion.HTTP_1_1, HttpVersion.HTTP_2);
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
                HttpOptions.HTTP_CLIENT_ENABLE_CONNECTION_POOL,
                HttpOptions.HTTP_CLIENT_CONNECTION_POOL_CAPACITY,
                HttpOptions.HTTP_CLIENT_CONNECTION_TTL
        );
        // build real client
        rebuildRealClient();
    }

    public void rebuildRealClient() {
        // default request config
        RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom();
        int connectTimeoutToUse = getConnectTimeout();
        if (connectTimeoutToUse > 0) {
            defaultRequestConfigBuilder.setConnectTimeout(connectTimeoutToUse);
        }
        int socketTimeoutToUse = getSocketTimeout();
        if (socketTimeoutToUse > 0) {
            defaultRequestConfigBuilder.setSocketTimeout(socketTimeoutToUse);
        }
        int connectionRequestTimeoutToUse = getConnectionRequestTimeout();
        if (connectionRequestTimeoutToUse > 0) {
            defaultRequestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeoutToUse);
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
            httpClientBuilder.evictIdleConnections(connectionIdleTimeToUse, TimeUnit.MILLISECONDS);
        }
        realHttpClient = httpClientBuilder.build();
    }

    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               RequestPayloadSupport payloadPublisher,
                                               ResponsePayloadSupport<?> payloadSubscriber) throws IOException {
        ResponsePayloadSupport<T> responsePayloadSupport = (ResponsePayloadSupport<T>) payloadSubscriber;
        HttpUriRequest httpUriRequest = createHttpUriRequest(request, payloadPublisher);
        CloseableHttpResponse closeableHttpResponse = realHttpClient.execute(httpUriRequest);
        try {
            return parseFromCloseableHttpResponse(closeableHttpResponse, responsePayloadSupport);
        } finally {
            IoUtils.X.closeQuietly(closeableHttpResponse);
        }
    }

    private HttpUriRequest createHttpUriRequest(HttpRequest request,
                                                RequestPayloadSupport payloadPublisher) {
        RequestBuilder builder = RequestBuilder.create(request.getRawMethod()).setUri(request.getUri());
        request.consumeHeaders(httpHeader -> builder.addHeader(httpHeader.name(), httpHeader.value()));
        org.apache.http.entity.ContentType contentType = null;
        String contentTypeValue = request.getContentTypeValue();
        if (Objects.nonNull(contentTypeValue)) {
            contentType = org.apache.http.entity.ContentType.parse(contentTypeValue);
        }
        if (request.supportPayload() || supportHttpOption(HttpOptions.HTTP_FORCE_WRITE)) {
            long contentLength = request.getContentLength();
            if (contentLength < 0) {
                contentLength = payloadPublisher.contentLength();
            }
            if (payloadPublisher instanceof PayloadPublisher) {
                PayloadPublisher publisher = (PayloadPublisher) payloadPublisher;
                if (contentLength < 0) {
                    builder.setEntity(PayloadPublisherEntityV4.newInstance(publisher, contentType));
                } else {
                    builder.setEntity(PayloadPublisherEntityV4.newInstance(publisher, contentLength, contentType));
                }
            } else if (payloadPublisher instanceof FlowPayloadPublisher) {
                FlowPayloadPublisher flowPublisher = (FlowPayloadPublisher) payloadPublisher;
                if (contentLength < 0) {
                    builder.setEntity(FlowPayloadPublisherEntityV4.newInstance(flowPublisher, contentType));
                } else {
                    builder.setEntity(FlowPayloadPublisherEntityV4.newInstance(flowPublisher, contentLength,
                            contentType));
                }
            }
        }
        Integer connectionRequestTimeoutToUse
                = request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_CONNECTION_REQUEST_TIMEOUT);
        Integer connectionTimeoutToUse
                = request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_CONNECT_TIMEOUT);
        Integer socketTimeoutToUse
                = request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_SOCKET_TIMEOUT);
        if (Objects.nonNull(connectionRequestTimeoutToUse) || Objects.nonNull(connectionTimeoutToUse) ||
                Objects.nonNull(socketTimeoutToUse)) {
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            Optional.ofNullable(connectionRequestTimeoutToUse).ifPresent(requestConfigBuilder::setConnectionRequestTimeout);
            Optional.ofNullable(connectionTimeoutToUse).ifPresent(requestConfigBuilder::setConnectTimeout);
            Optional.ofNullable(socketTimeoutToUse).ifPresent(requestConfigBuilder::setSocketTimeout);
            builder.setConfig(requestConfigBuilder.build());
        }
        return builder.build();
    }

    private <T> HttpResponse<T> parseFromCloseableHttpResponse(CloseableHttpResponse closeableHttpResponse,
                                                               ResponsePayloadSupport<T> responsePayloadSupport) throws IOException {
        HttpEntity responseEntity = closeableHttpResponse.getEntity();
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
        StatusLine statusLine = closeableHttpResponse.getStatusLine();
        DefaultHttpResponse<T> httpResponse = new DefaultHttpResponse<>(responsePayloadSupport.getPayload(),
                statusLine.getStatusCode());
        httpResponse.setReasonPhrase(statusLine.getReasonPhrase());
        HttpVersion httpVersion = HttpVersion.defaultVersion();
        ProtocolVersion protocolVersion = statusLine.getProtocolVersion();
        HttpVersion parsedHttpVersion = HttpVersion.fromVersion(protocolVersion.getMajor(), protocolVersion.getMinor());
        if (Objects.nonNull(parsedHttpVersion)) {
            httpVersion = parsedHttpVersion;
        }
        httpResponse.setProtocolVersion(httpVersion);
        HeaderIterator headerIterator = closeableHttpResponse.headerIterator();
        while (headerIterator.hasNext()) {
            Header responseHeader = headerIterator.nextHeader();
            httpResponse.addHeader(responseHeader.getName(), responseHeader.getValue());
        }
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

    public int getConnectionRequestTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECTION_REQUEST_TIMEOUT))
                .orElse(this.connectionRequestTimeout);
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
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

    @Override
    protected void closeInternal() throws IOException {
        Optional.ofNullable(realHttpClient).ifPresent(ahc -> {
            Optional.ofNullable(connectionManager).ifPresent(HttpClientConnectionManager::shutdown);
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
        ReflectionUtils.X.forName("org.apache.http.impl.client.CloseableHttpClient");
    }
}
