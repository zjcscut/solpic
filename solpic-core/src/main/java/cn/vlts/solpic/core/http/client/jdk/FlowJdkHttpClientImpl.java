package cn.vlts.solpic.core.http.client.jdk;

import cn.vlts.solpic.core.common.HttpHeaderConstants;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.impl.DefaultHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Default JDK HTTP client, base on HttpURLConnection.
 *
 * @author throwable
 * @since 2024/7/24 00:27
 */
public class FlowJdkHttpClientImpl extends BaseHttpClient<FlowPayloadPublisher, FlowPayloadSubscriber<?>>
        implements HttpClient<FlowPayloadPublisher, FlowPayloadSubscriber<?>>, HttpOptional {

    private Proxy proxy;

    private int connectTimeout = -1;

    private int readTimeout = -1;

    private int chunkSize = 4 * 1024;

    public FlowJdkHttpClientImpl() {
        super();
        init();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               FlowPayloadPublisher payloadPublisher,
                                               FlowPayloadSubscriber<?> payloadSubscriber) throws IOException {
        FlowPayloadSubscriber<T> subscriber = (FlowPayloadSubscriber<T>) payloadSubscriber;
        // create connection
        HttpURLConnection httpConnection = createHttpConnection(request);
        if (httpConnection.getDoOutput()) {
            long contentLength = request.getContentLength();
            if (contentLength > 0) {
                httpConnection.setFixedLengthStreamingMode(contentLength);
            } else {
                httpConnection.setChunkedStreamingMode(this.chunkSize);
            }
        }
        // process request headers
        populateHeaders(httpConnection, request);
        // connect
        httpConnection.connect();
        // write request body
        if (httpConnection.getDoOutput()) {
            OutputStream outputStream = httpConnection.getOutputStream();
            // todo write
        } else {
            httpConnection.getResponseCode();
        }
        // read response body
        InputStream errorStream = httpConnection.getErrorStream();
        InputStream inputStream = httpConnection.getInputStream();
        InputStream responseStream = Objects.nonNull(errorStream) ? errorStream : inputStream;
        if (Objects.nonNull(responseStream)) {
            // todo read
        }
        int responseCode = httpConnection.getResponseCode();
        DefaultHttpResponse<T> httpResponse = new DefaultHttpResponse<>(subscriber.getPayload(), responseCode);
        // process response
        populateResponse(httpResponse, httpConnection, request);
        return httpResponse;
    }

    private void init() {
        // HttpURLConnection only support HTTP/1.0 and HTTP/1.1
        addHttpVersions(HttpVersion.HTTP_1, HttpVersion.HTTP_1_1);
    }

    private HttpURLConnection createHttpConnection(HttpRequest request) throws IOException {
        URL url = request.getUri().toURL();
        URLConnection urlConnection = Objects.nonNull(this.proxy) ? url.openConnection(this.proxy) : url.openConnection();
        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IllegalStateException("Require HttpURLConnection, but got: " + urlConnection);
        }
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
        if (this.connectTimeout > 0) {
            httpConnection.setConnectTimeout(this.connectTimeout);
        }
        if (this.readTimeout > 0) {
            httpConnection.setReadTimeout(this.readTimeout);
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
}
