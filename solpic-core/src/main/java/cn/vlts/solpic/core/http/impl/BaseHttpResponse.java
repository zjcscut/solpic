package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.common.HttpStatusCode;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.HttpResponse;

import java.util.Objects;

/**
 * Base HTTP response.
 *
 * @author throwable
 * @since 2024/7/28 20:01
 */
public abstract class BaseHttpResponse<T> extends HttpMessageSupport implements HttpResponse<T> {

    private HttpStatusCode httpStatusCode;

    private HttpClient httpClient;

    private HttpRequest httpRequest;

    private String reasonPhrase;

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int statusCode) {
        this.httpStatusCode = HttpStatusCode.fromStatusCode(statusCode);
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public String getReasonPhrase() {
        return Objects.nonNull(this.reasonPhrase) ? this.reasonPhrase : HttpResponse.super.getReasonPhrase();
    }
}
