package cn.vlts.solpic.core.http.client.ahc5;

import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.http.flow.ByteBufferConsumerOutputStream;
import cn.vlts.solpic.core.http.flow.FlowOutputStreamSubscriber;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.PullingInputStream;
import cn.vlts.solpic.core.util.IoUtils;
import cn.vlts.solpic.core.util.ReflectionUtils;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.NullEntity;

import java.io.IOException;
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

    private int timeout = -1;

    private int requestTimeout = -1;

    private HttpClientConnectionManager connectionManager;

    private CloseableHttpClient realHttpClient;

    public ApacheHttpClientV5Impl() {
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
                HttpOptions.HTTP_CLIENT_ENABLE_CONNECTION_POOL,
                HttpOptions.HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE,
                HttpOptions.HTTP_CLIENT_CONNECTION_POOL_TTL
        );

        ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        int connectTimeoutToUse = getConnectTimeout();
        if (connectTimeoutToUse >= 0) {

        }
        realHttpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfigBuilder.build())
                .build();
    }

    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               RequestPayloadSupport payloadPublisher,
                                               ResponsePayloadSupport<?> payloadSubscriber) throws IOException {
        ResponsePayloadSupport<T> responsePayloadSupport = (ResponsePayloadSupport<T>) payloadSubscriber;

        return null;
    }

    private ClassicHttpRequest createClassicHttpRequest(HttpRequest request,
                                                        RequestPayloadSupport payloadPublisher) throws IOException {
        HttpMethod method = request.getMethod();
        HttpUriRequestBase base = new HttpUriRequestBase(method.name(), request.getUri());
//        Integer requestTimeout = request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_TIMEOUT);
//        if (Objects.nonNull(requestTimeout)) {
//            RequestConfig requestConfig = RequestConfig.custom()
//                    .setResponseTimeout(requestTimeout, TimeUnit.MILLISECONDS)
//                    .build();
//            base.setConfig(requestConfig);
//        }
        String contentTypeValue = request.getContentTypeValue();
        org.apache.hc.core5.http.ContentType contentType = null;
        if (Objects.nonNull(contentTypeValue)) {
            contentType = org.apache.hc.core5.http.ContentType.parse(contentTypeValue);
        }
        if (request.supportPayload() || supportHttpOption(HttpOptions.HTTP_FORCE_WRITE)) {
            EntityBuilder bodyBuilder = EntityBuilder.create();
            ByteBufferConsumerOutputStream outputStream = new ByteBufferConsumerOutputStream(null);
            if (payloadPublisher instanceof PayloadPublisher) {
                ((PayloadPublisher) payloadPublisher).writeTo(outputStream);
            } else if (payloadPublisher instanceof FlowPayloadPublisher) {
                FlowPayloadPublisher flowPublisher = (FlowPayloadPublisher) payloadPublisher;
                flowPublisher.subscribe(FlowOutputStreamSubscriber.ofOutputStream(outputStream));
            }
            PullingInputStream pullingInputStream = new PullingInputStream(null);
            InputStreamEntity inputStreamEntity = new InputStreamEntity(pullingInputStream, contentType);
            base.setEntity(inputStreamEntity);
        }
        return base;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECT_TIMEOUT))
                .orElse(this.connectTimeout);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_TIMEOUT))
                .orElse(this.timeout);
    }

    @Override
    protected void closeInternal() {
        Optional.ofNullable(realHttpClient).ifPresent(hc -> {
            Optional.ofNullable(connectionManager).ifPresent(IoUtils.X::closeQuietly);
            IoUtils.X.closeQuietly(hc);
        });
    }

    static {
        ReflectionUtils.X.forName("org.apache.hc.client5.http.impl.classic.CloseableHttpClient");
    }
}
