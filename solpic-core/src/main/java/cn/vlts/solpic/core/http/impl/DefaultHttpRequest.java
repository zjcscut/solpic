package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.common.HttpRequestStatus;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.config.HttpOptionParser;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.config.OptionLevel;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.util.ArgumentUtils;
import cn.vlts.solpic.core.util.AttachmentKey;
import cn.vlts.solpic.core.util.Pair;
import cn.vlts.solpic.core.util.UriBuilder;

import java.net.URI;
import java.util.*;

/**
 * Default HTTP request.
 *
 * @author throwable
 * @since 2024/7/28 22:33
 */
public class DefaultHttpRequest extends BaseHttpRequest implements HttpRequest {

    protected final List<HttpVersion> httpVersions = new ArrayList<>();

    protected final Map<HttpOption<?>, Object> options = new HashMap<>();

    protected long availableOpts = -1;

    protected long minimumOpts = -1;

    private volatile HttpRequestStatus status = HttpRequestStatus.INIT;

    private HttpClient httpClient;

    private RequestPayloadSupport requestPayloadSupport;

    public DefaultHttpRequest(HttpMethod method) {
        super(method);
        this.httpClient = null;
    }

    public DefaultHttpRequest(String method) {
        super(HttpMethod.fromMethod(method));
        this.httpClient = null;
    }

    public DefaultHttpRequest(HttpMethod method, HttpClient httpClient) {
        super(method);
        this.httpClient = httpClient;
    }

    public DefaultHttpRequest(String method, HttpClient httpClient) {
        super(HttpMethod.fromMethod(method));
        this.httpClient = httpClient;
    }

    public DefaultHttpRequest(HttpMethod method, URI uri) {
        this(method, uri, null);
    }

    public DefaultHttpRequest(String method, URI uri) {
        this(method, uri, null);
    }

    public DefaultHttpRequest(HttpMethod method, URI uri, HttpClient httpClient) {
        super(method, uri);
        this.httpClient = httpClient;
    }

    public DefaultHttpRequest(String method, URI uri, HttpClient httpClient) {
        super(HttpMethod.fromMethod(method), uri);
        this.httpClient = httpClient;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public boolean supportHttpVersion(HttpVersion httpVersion) {
        return httpVersions.stream().anyMatch(hv -> hv.isSameAs(httpVersion));
    }

    @Override
    public List<HttpVersion> availableHttpVersions() {
        return Collections.unmodifiableList(httpVersions);
    }

    @Override
    public boolean supportHttpOption(HttpOption<?> httpOption) {
        return this.options.containsKey(httpOption);
    }

    @Override
    public List<HttpOption<?>> getAvailableHttpOptions() {
        return this.availableOpts == -1 ? Collections.emptyList() : HttpOptions.getMatchedOptions(this.availableOpts);
    }

    @Override
    public List<HttpOption<?>> getMinimumHttpOptions() {
        return this.minimumOpts == -1 ? Collections.emptyList() : HttpOptions.getMatchedOptions(this.minimumOpts);
    }

    @Override
    public List<HttpOption<?>> getHttpOptions() {
        return Collections.unmodifiableList(new ArrayList<>(this.options.keySet()));
    }

    @Override
    public <T> T getHttpOptionValue(HttpOption<T> httpOption) {
        if (!supportHttpOption(httpOption)) {
            return null;
        }
        if (OptionLevel.REQUEST == httpOption.level()) {
            Class<T> type = httpOption.valueType();
            T configValue = type.cast(options.get(httpOption));
            return HttpOptionParser.X.parseOptionValue(httpOption, configValue);
        }
        return Optional.ofNullable(httpClient).map(hc -> hc.getHttpOptionValue(httpOption)).orElse(null);
    }

    public void addHttpVersion(HttpVersion httpVersion) {
        this.httpVersions.add(httpVersion);
    }

    public void addHttpVersions(HttpVersion... httpVersions) {
        if (Objects.nonNull(httpVersions)) {
            this.httpVersions.addAll(Arrays.asList(httpVersions));
        }
    }

    public void addAvailableHttpOption(HttpOption<?> httpOption) {
        ArgumentUtils.X.isTrue(httpOption.level() == OptionLevel.REQUEST,
                "Option level must be REQUEST for option: " + httpOption.key());
        this.availableOpts |= httpOption.id();
    }

    public void addMinimumHttpOption(HttpOption<?> httpOption) {
        ArgumentUtils.X.isTrue(httpOption.level() == OptionLevel.REQUEST,
                "Option level must be REQUEST for option: " + httpOption.key());
        this.minimumOpts |= httpOption.id();
    }

    public <T> void addHttpOption(HttpOption<T> httpOption, T configValue) {
        ArgumentUtils.X.isTrue(httpOption.level() == OptionLevel.REQUEST,
                "Option level must be REQUEST for option: " + httpOption.key());
        this.options.putIfAbsent(httpOption, configValue);
    }

    public <T> void setHttpOption(HttpOption<T> httpOption, T configValue) {
        ArgumentUtils.X.isTrue(httpOption.level() == OptionLevel.REQUEST,
                "Option level must be REQUEST for option: " + httpOption.key());
        this.options.put(httpOption, configValue);
    }

    @Override
    public void validateMinimumHttpOptions() {
        if (minimumOpts != -1) {
            List<HttpOption<?>> minimumOptions = HttpOptions.getMatchedOptions(minimumOpts);
            Set<HttpOption<?>> optionsToUse = options.keySet();
            for (HttpOption<?> minimumOption : minimumOptions) {
                if (!optionsToUse.contains(minimumOption)) {
                    throw new IllegalArgumentException("HttpOption '" + minimumOption + "' is required");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends RequestPayloadSupport> S getPayloadPublisher() {
        return (S) requestPayloadSupport;
    }

    public <S extends RequestPayloadSupport> void setPayloadPublisher(S payloadPublisher) {
        this.requestPayloadSupport = payloadPublisher;
    }

    public void changeStatus(HttpRequestStatus status) {
        if (!Objects.equals(this.status, HttpRequestStatus.ABORTED)) {
            this.status = status;
        }
    }

    @Override
    public HttpRequestStatus getStatus() {
        return this.status;
    }

    @Override
    public void abort() {
        // only change request status to 'ABORTED', check it before send
        changeStatus(HttpRequestStatus.ABORTED);
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static class Builder implements HttpRequest.Builder {

        private final List<Pair> queryParams = new ArrayList<>();

        private final List<String> paths = new ArrayList<>();

        private final List<HttpHeader> headers = new ArrayList<>();

        private final Map<HttpOption<?>, Object> options = new LinkedHashMap<>();

        private final Map<AttachmentKey, Object> attachments = new LinkedHashMap<>();

        private final List<HttpOption<?>> availableOptions = new ArrayList<>();

        private final List<HttpOption<?>> minimumOptions = new ArrayList<>();

        private RequestPayloadSupport payloadPublisher = PayloadPublishers.X.discarding();

        private URI uri;

        private HttpMethod method = HttpMethod.GET;

        @Override
        public HttpRequest.Builder minimumOption(HttpOption<?> httpOption) {
            ArgumentUtils.X.notNull("httpOption", httpOption);
            ArgumentUtils.X.isTrue(httpOption.level() == OptionLevel.REQUEST,
                    "Option level must be REQUEST for option: " + httpOption.id());
            this.minimumOptions.add(httpOption);
            return this;
        }

        @Override
        public HttpRequest.Builder availableOption(HttpOption<?> httpOption) {
            ArgumentUtils.X.notNull("httpOption", httpOption);
            ArgumentUtils.X.isTrue(httpOption.level() == OptionLevel.REQUEST,
                    "Option level must be REQUEST for option: " + httpOption.id());
            this.availableOptions.add(httpOption);
            return this;
        }

        @Override
        public <H> HttpRequest.Builder option(HttpOption<H> httpOption, H value) {
            ArgumentUtils.X.notNull("httpOption", httpOption);
            ArgumentUtils.X.notNull("value", value);
            ArgumentUtils.X.isTrue(httpOption.level() == OptionLevel.REQUEST,
                    "Option level must be REQUEST for option: " + httpOption.id());
            this.options.put(httpOption, value);
            return this;
        }

        @Override
        public <A> HttpRequest.Builder attachment(AttachmentKey key, A value) {
            ArgumentUtils.X.notNull("key", key);
            ArgumentUtils.X.notNull("value", value);
            this.attachments.put(key, value);
            return this;
        }

        @Override
        public Builder header(HttpHeader header) {
            ArgumentUtils.X.notNull("header", header);
            this.headers.add(header);
            return this;
        }

        @Override
        public Builder header(String name, String value) {
            ArgumentUtils.X.notNull("name", name);
            ArgumentUtils.X.notNull("value", value);
            this.headers.add(HttpHeader.of(name, value));
            return this;
        }

        @Override
        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public HttpRequest.Builder method(HttpMethod method) {
            ArgumentUtils.X.notNull("method", method);
            this.method = method;
            return this;
        }

        @Override
        public Builder query(String name, String value) {
            ArgumentUtils.X.notNull("name", name);
            ArgumentUtils.X.notNull("value", value);
            this.queryParams.add(Pair.of(name, value));
            return this;
        }

        @Override
        public HttpRequest.Builder path(String path) {
            ArgumentUtils.X.notNull("path", path);
            this.paths.add(path);
            return this;
        }

        @Override
        public <S extends RequestPayloadSupport> Builder payloadPublisher(S payloadPublisher) {
            this.payloadPublisher = payloadPublisher;
            return this;
        }

        @Override
        public HttpRequest build() {
            ArgumentUtils.X.notNull("uri", uri);
            UriBuilder uriBuilder = UriBuilder.newInstance(uri);
            for (String path : paths) {
                uriBuilder.appendPath(path);
            }
            for (Pair queryParam : queryParams) {
                uriBuilder.addQueryParameter(queryParam.name(), queryParam.value());
            }
            DefaultHttpRequest request = new DefaultHttpRequest(method, uriBuilder.build());
            for (HttpHeader header : headers) {
                request.addHeader(header);
            }
            for (HttpOption<?> option : minimumOptions) {
                request.addMinimumHttpOption(option);
            }
            for (HttpOption<?> option : availableOptions) {
                request.addAvailableHttpOption(option);
            }
            for (Map.Entry<HttpOption<?>, Object> option : options.entrySet()) {
                HttpOption httpOption = option.getKey();
                request.addHttpOption(httpOption, option.getValue());
            }
            for (Map.Entry<AttachmentKey, Object> attachment : attachments.entrySet()) {
                request.addAttachment(attachment.getKey(), attachment.getValue());
            }
            request.setPayloadPublisher(payloadPublisher);
            return request;
        }
    }
}
