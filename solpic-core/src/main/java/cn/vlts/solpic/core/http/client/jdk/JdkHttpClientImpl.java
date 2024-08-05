package cn.vlts.solpic.core.http.client.jdk;

import cn.vlts.solpic.core.common.HttpHeaderConstants;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.http.flow.FlowInputStreamPublisher;
import cn.vlts.solpic.core.http.flow.FlowOutputStreamSubscriber;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.impl.DefaultHttpResponse;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * JDK HTTP client, base on HttpURLConnection.
 *
 * @author throwable
 * @since 2024/7/24 00:27
 */
public class JdkHttpClientImpl extends BaseHttpClient implements HttpClient, HttpOptional {

    private static final int DEFAULT_CHUNK_SIZE = 4 * 1024;

    private int connectTimeout = -1;

    private int readTimeout = -1;

    private int chunkSize = DEFAULT_CHUNK_SIZE;

    public JdkHttpClientImpl() {
        super();
        init();
    }

    private void init() {
        // HttpURLConnection only support HTTP/1.0 and HTTP/1.1
        addHttpVersions(HttpVersion.HTTP_1, HttpVersion.HTTP_1_1);
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
                HttpOptions.HTTP_REQUEST_CONNECT_TIMEOUT,
                HttpOptions.HTTP_READ_TIMEOUT,
                HttpOptions.HTTP_REQUEST_READ_TIMEOUT,
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
            long contentLength = payloadPublisher.contentLength();
            if (contentLength < 0) {
                contentLength = request.getContentLength();
            }
            if (contentLength > 0) {
                httpConnection.setFixedLengthStreamingMode(contentLength);
            } else {
                int chunkSizeToUse = getChunkSize();
                httpConnection.setChunkedStreamingMode(chunkSizeToUse);
            }
        }
        // process request headers
        populateHeaders(httpConnection, request);
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
        // read response body
        InputStream errorStream = httpConnection.getErrorStream();
        InputStream inputStream = httpConnection.getInputStream();
        InputStream responseStream = Objects.nonNull(errorStream) ? errorStream : inputStream;
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
        int responseCode = httpConnection.getResponseCode();
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
        int connectTimeoutToUse = getConnectTimeout();
        if (connectTimeoutToUse > 0) {
            httpConnection.setConnectTimeout(connectTimeoutToUse);
        }
        int readTimeoutToUse = getReadTimeout();
        if (readTimeoutToUse > 0) {
            httpConnection.setReadTimeout(readTimeoutToUse);
        }
        httpConnection.setRequestMethod(request.getRawMethod());
        httpConnection.setDoInput(true);
        if (request.supportPayload() || request.supportHttpOption(HttpOptions.HTTP_FORCE_WRITE)) {
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
        if (Objects.nonNull(firstName) && !firstName.isEmpty()) {
            httpResponse.setHeader(firstName, connection.getHeaderField(0));
        }
        int index = 1;
        for (; ; ) {
            String headerName = connection.getHeaderFieldKey(index);
            if (Objects.isNull(headerName) || headerName.isEmpty()) {
                break;
            }
            httpResponse.addHeader(headerName, connection.getHeaderField(headerName));
            index++;
        }
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_REQUEST_CONNECT_TIMEOUT))
                .orElse(Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CONNECT_TIMEOUT))
                        .orElse(this.connectTimeout));
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getReadTimeout() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_REQUEST_READ_TIMEOUT))
                .orElse(Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_READ_TIMEOUT))
                        .orElse(this.readTimeout));
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {
        return Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_REQUEST_CHUNK_SIZE))
                .orElse(Optional.ofNullable(getHttpOptionValue(HttpOptions.HTTP_CHUNK_SIZE))
                        .orElse(this.chunkSize));
    }
}
