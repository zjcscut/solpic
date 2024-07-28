package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.common.UriScheme;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.util.UriBuilder;

import java.net.URI;
import java.util.Objects;

/**
 * Base HTTP request.
 *
 * @author throwable
 * @since 2024/7/23 星期二 19:57
 */
public abstract class BaseHttpRequest extends HttpMessageSupport implements HttpRequest {

    private final HttpMethod method;

    private String scheme = UriScheme.HTTP.getValue();

    private URI uri;

    private String rawUri;

    protected BaseHttpRequest(HttpMethod method) {
        this.method = method;
    }

    protected BaseHttpRequest(HttpMethod method, URI uri) {
        this.method = method;
        setUri(uri);
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    @Override
    public void setScheme(String scheme) {
        this.scheme = scheme;
        if (Objects.nonNull(this.uri)) {
            this.uri = UriBuilder.newInstance(this.uri).withScheme(scheme).build();
        }
    }

    @Override
    public String getRawMethod() {
        return this.method.toString();
    }

    @Override
    public HttpMethod getMethod() {
        return this.method;
    }

    @Override
    public void setRawUri(String uri) {
        this.uri = URI.create(uri);
        this.rawUri = uri;
    }

    @Override
    public String getRawUri() {
        return this.rawUri;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public void setUri(URI uri) {
        this.uri = uri;
        if (Objects.nonNull(uri)) {
            this.scheme = uri.getScheme();
            this.rawUri = uri.toString();
        }
    }
}
