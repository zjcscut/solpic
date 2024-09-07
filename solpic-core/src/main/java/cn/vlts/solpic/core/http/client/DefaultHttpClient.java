package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.common.HttpHeaderConstants;
import cn.vlts.solpic.core.common.HttpStatus;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.config.SSLConfig;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.flow.FlowInputStreamPublisher;
import cn.vlts.solpic.core.http.flow.FlowOutputStreamSubscriber;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.impl.DefaultHttpResponse;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.util.ArgumentUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Default HTTP client, base on HttpURLConnection.
 *
 * @author throwable
 * @since 2024/7/24 00:27
 */
public class DefaultHttpClient extends BaseHttpClient implements HttpClient, HttpOptional {

    private static final int DEFAULT_CHUNK_SIZE = 4 * 1024;

    private int connectTimeout = -1;

    private int readTimeout = -1;

    private int chunkSize = DEFAULT_CHUNK_SIZE;

    public DefaultHttpClient() {
        super();
    }

    @Override
    protected void initInternal() {
        // HttpURLConnection only support HTTP/1.0 and HTTP/1.1
        addHttpVersions(HttpVersion.HTTP_1, HttpVersion.HTTP_1_1);
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
                HttpOptions.HTTP_CONNECT_TIMEOUT,
                HttpOptions.HTTP_READ_TIMEOUT,
                HttpOptions.HTTP_CHUNK_SIZE
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               RequestPayloadSupport payloadPublisher,
                                               ResponsePayloadSupport<?> payloadSubscriber) throws IOException {
        ResponsePayloadSupport<T> responsePayloadSupport = (ResponsePayloadSupport<T>) payloadSubscriber;
        // create connection
        HttpURLConnection httpConnection = createHttpConnection(request);
        if (httpConnection.getDoOutput()) {
            long contentLength = request.getContentLength();
            if (contentLength < 0) {
                contentLength = payloadPublisher.contentLength();
            }
            if (contentLength >= 0) {
                httpConnection.setFixedLengthStreamingMode(contentLength);
            } else {
                int chunkSizeToUse = getChunkSize(request);
                httpConnection.setChunkedStreamingMode(chunkSizeToUse);
            }
        }
        // process request headers
        populateHeaders(httpConnection, request);
        httpConnection.setUseCaches(false);
        // connect
        httpConnection.connect();
        // write request body
        if (httpConnection.getDoOutput()) {
            OutputStream outputStream = httpConnection.getOutputStream();
            if (payloadPublisher instanceof PayloadPublisher) {
                ((PayloadPublisher) payloadPublisher).writeTo(outputStream);
            } else if (payloadPublisher instanceof FlowPayloadPublisher) {
                FlowPayloadPublisher flowPublisher = (FlowPayloadPublisher) payloadPublisher;
                flowPublisher.subscribe(FlowOutputStreamSubscriber.ofOutputStream(outputStream));
            }
        } else {
            httpConnection.getResponseCode();
        }
        int responseCode = httpConnection.getResponseCode();
        // read response body
        InputStream responseStream = responseCode >= HttpStatus.BAD_REQUEST.value() ? httpConnection.getErrorStream() :
                httpConnection.getInputStream();
        if (Objects.nonNull(responseStream)) {
            if (responsePayloadSupport instanceof PayloadSubscriber) {
                PayloadSubscriber<T> subscriber = (PayloadSubscriber<T>) responsePayloadSupport;
                subscriber.readFrom(responseStream);
            } else if (responsePayloadSupport instanceof FlowPayloadSubscriber) {
                FlowPayloadSubscriber<T> flowSubscriber = (FlowPayloadSubscriber<T>) responsePayloadSupport;
                FlowInputStreamPublisher.ofInputStream(responseStream).subscribe(flowSubscriber);
            }
        } else {
            // force to discard
            responsePayloadSupport = PayloadSubscribers.X.discarding();
        }
        DefaultHttpResponse<T> httpResponse = new DefaultHttpResponse<>(responsePayloadSupport.getPayload(), responseCode);
        // process response
        populateResponse(httpResponse, httpConnection, request);
        return httpResponse;
    }

    private HttpURLConnection createHttpConnection(HttpRequest request) throws IOException {
        URL url = request.getUri().toURL();
        Proxy proxyToUse = getProxy();
        URLConnection urlConnection = Objects.nonNull(proxyToUse) ? url.openConnection(proxyToUse) : url.openConnection();
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IllegalStateException("Require HttpURLConnection, but got: " + urlConnection);
        }
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
        SSLConfig sslConfigToUse = getHttpOptionValue(HttpOptions.HTTP_SSL_CONFIG);
        if (Objects.nonNull(sslConfigToUse) &&
                !Objects.equals(sslConfigToUse, SSLConfig.NO) &&
                httpConnection instanceof HttpsURLConnection) {
            if (Objects.nonNull(sslConfigToUse.getContext())) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) httpConnection;
                HostnameVerifier hostnameVerifier = sslConfigToUse.getHostnameVerifier();
                if (Objects.isNull(hostnameVerifier)) {
                    hostnameVerifier = (hostname, session) -> true;
                }
                SSLSocketFactory socketFactory = sslConfigToUse.getContext().getSocketFactory();
                httpsConnection.setSSLSocketFactory(socketFactory);
                httpsConnection.setHostnameVerifier(hostnameVerifier);
            }
        }
        int connectTimeoutToUse = getConnectTimeout(request);
        if (connectTimeoutToUse > 0) {
            httpConnection.setConnectTimeout(connectTimeoutToUse);
        }
        int readTimeoutToUse = getReadTimeout(request);
        if (readTimeoutToUse > 0) {
            httpConnection.setReadTimeout(readTimeoutToUse);
        }
        httpConnection.setRequestMethod(request.getRawMethod());
        httpConnection.setDoInput(true);
        if (request.supportPayload() || isForceWriteRequestPayload(request)) {
            httpConnection.setDoOutput(true);
        }
        httpConnection.setInstanceFollowRedirects(Objects.equals(request.getMethod(), HttpMethod.GET));
        return httpConnection;
    }

    private void populateHeaders(HttpURLConnection connection, HttpRequest request) {
        Set<String> headerNames = request.getAllHeaderNames();
        for (String headerName : headerNames) {
            List<String> headerValues = request.getHeaderValue(headerName);
            if (Objects.nonNull(headerValues)) {
                if (HttpHeaderConstants.COOKIE_KEY.equalsIgnoreCase(headerName)) {
                    StringJoiner joiner = new StringJoiner("; ");
                    for (String headerValue : headerValues) {
                        joiner.add(headerValue);
                    }
                    connection.setRequestProperty(headerName, joiner.toString());
                } else {
                    for (String headerValue : headerValues) {
                        connection.addRequestProperty(headerName, headerValue);
                    }
                }
            }
        }
    }

    private <T> void populateResponse(DefaultHttpResponse<T> httpResponse,
                                      HttpURLConnection connection,
                                      HttpRequest request) throws IOException {
        httpResponse.setHttpRequest(request);
        httpResponse.setHttpClient(this);
        httpResponse.setReasonPhrase(connection.getResponseMessage());
        // process response headers - first line should be status text line
        String firstName = connection.getHeaderFieldKey(0);
        String statusLine = connection.getHeaderField(0);
        if (ArgumentUtils.X.hasLength(firstName) && ArgumentUtils.X.hasLength(statusLine)) {
            httpResponse.addHeader(HttpHeaderConstants.HTTP_URL_CONNECTION_STATUS_LINE_KEY, statusLine);
            HttpVersion httpVersion = HttpVersion.fromStatusLine(statusLine);
            httpResponse.setProtocolVersion(httpVersion);
        }
        int index = 1;
        for (; ; ) {
            String headerName = connection.getHeaderFieldKey(index);
            if (!ArgumentUtils.X.hasLength(headerName)) {
                break;
            }
            httpResponse.addHeader(headerName, connection.getHeaderField(headerName));
            index++;
        }
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout(HttpRequest request) {
        return Optional.ofNullable(request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_CONNECT_TIMEOUT))
                .orElse(Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECT_TIMEOUT))
                        .orElse(this.connectTimeout));
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getReadTimeout(HttpRequest request) {
        return Optional.ofNullable(request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_READ_TIMEOUT))
                .orElse(Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_READ_TIMEOUT))
                        .orElse(this.readTimeout));
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkSize(HttpRequest request) {
        return Optional.ofNullable(request.getHttpOptionValue(HttpOptions.HTTP_REQUEST_CHUNK_SIZE))
                .orElse(Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CHUNK_SIZE))
                        .orElse(this.chunkSize));
    }
}
