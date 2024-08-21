package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.codec.CodecFactory;
import cn.vlts.solpic.core.codec.CodecType;
import cn.vlts.solpic.core.common.HttpClientType;
import cn.vlts.solpic.core.config.*;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.bind.ApiEnhancerBuilder;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.http.client.HttpClientFactory;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.HttpOptionSupport;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.http.impl.ReadOnlyHttpResponse;
import cn.vlts.solpic.core.http.interceptor.HttpInterceptor;
import cn.vlts.solpic.core.util.ArgumentUtils;

import java.net.URI;
import java.util.*;
import java.util.function.Function;

/**
 * The solpic.
 *
 * @author throwable
 * @since 2024/7/28 23:27
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Solpic {

    private Solpic() {
        throw new Error();
    }

    public static HttpClientBuilder newHttpClientBuilder() {
        return new DefaultHttpClientBuilder();
    }

    public static SolpicTemplate newSolpicTemplate() {
        return new DefaultSolpicTemplateBuilder()
                .httpClient(newHttpClient())
                .codec(newCodec())
                .build();
    }

    public static SolpicTemplateBuilder newSolpicTemplateBuilder() {
        return new DefaultSolpicTemplateBuilder();
    }

    public static OneWaySolpicTemplate newOneWaySolpicTemplate() {
        return new DefaultOneWaySolpicTemplateBuilder()
                .httpClient(newHttpClient())
                .codec(newCodec())
                .build();
    }

    public static OneWaySolpicTemplateBuilder newOneWaySolpicTemplateBuilder() {
        return new DefaultOneWaySolpicTemplateBuilder();
    }

    public static <S, T> Codec<S, T> newCodec(String codecName) {
        ArgumentUtils.X.notNull("codecName", codecName);
        CodecType codecTypeToUse = null;
        for (CodecType codecType : CodecType.values()) {
            if (codecType.name().equalsIgnoreCase(codecName)) {
                codecTypeToUse = codecType;
                break;
            }
        }
        return CodecFactory.X.loadCodec(codecTypeToUse, codecName);
    }

    public static <S, T> Codec<S, T> newCodec() {
        return CodecFactory.X.loadBestMatchedCodec();
    }

    public static HttpClient newHttpClient(String httpClientName) {
        ArgumentUtils.X.notNull("httpClientName", httpClientName);
        return newHttpClientBuilder().name(httpClientName).build();
    }

    public static HttpClient newHttpClient() {
        return newHttpClientBuilder().build();
    }

    public static ApiEnhancerBuilder newApiEnhancerBuilder() {
        return ApiEnhancerBuilder.newBuilder();
    }

    public interface HttpClientBuilder {

        HttpClientBuilder name(String name);

        HttpClientBuilder type(HttpClientType httpClientType);

        <T> HttpClientBuilder option(HttpOption<T> option, T optionValue);

        HttpClientBuilder addMinimumOption(HttpOption<?> option);

        HttpClientBuilder addAvailableOption(HttpOption<?> option);

        HttpClientBuilder addInterceptor(HttpInterceptor interceptor);

        HttpClient build();
    }

    private static class DefaultHttpClientBuilder implements HttpClientBuilder {

        private String spiName;

        private HttpClientType clientType;

        private String optionClientType;

        private final Map<HttpOption, Object> options = new HashMap<>();

        private final List<HttpOption<?>> minimumOptions = new ArrayList<>();

        private final List<HttpOption<?>> availableOptions = new ArrayList<>();

        private final List<HttpInterceptor> interceptors = new ArrayList<>();

        @Override
        public HttpClientBuilder name(String name) {
            ArgumentUtils.X.notNull("name", name);
            this.spiName = name;
            return this;
        }

        @Override
        public HttpClientBuilder type(HttpClientType httpClientType) {
            ArgumentUtils.X.notNull("httpClientType", httpClientType);
            this.clientType = httpClientType;
            return this;
        }

        @Override
        public <T> HttpClientBuilder option(HttpOption<T> option, T optionValue) {
            ArgumentUtils.X.notNull("option", option);
            ArgumentUtils.X.notNull("optionValue", optionValue);
            if (option.id() == HttpOptions.HTTP_CLIENT_TYPE.id()) {
                this.optionClientType = (String) HttpOptionParser.X.parseOptionValue(option, optionValue);
                return this;
            } else {
                ArgumentUtils.X.isTrue(Objects.equals(option.level(), OptionLevel.CLIENT), "The level of HttpOption must be client");
            }
            this.options.put(option, optionValue);
            return this;
        }

        @Override
        public HttpClientBuilder addMinimumOption(HttpOption<?> option) {
            ArgumentUtils.X.notNull("option", option);
            this.minimumOptions.add(option);
            return this;
        }

        @Override
        public HttpClientBuilder addAvailableOption(HttpOption<?> option) {
            ArgumentUtils.X.notNull("option", option);
            this.availableOptions.add(option);
            return this;
        }

        @Override
        public HttpClientBuilder addInterceptor(HttpInterceptor interceptor) {
            ArgumentUtils.X.notNull("interceptor", interceptor);
            this.interceptors.add(interceptor);
            return this;
        }

        @Override
        public HttpClient build() {
            String httpClientName = Optional.ofNullable(spiName)
                    .orElse(Optional.ofNullable(optionClientType).orElse(clientType.getCode()));
            HttpClient httpClient = HttpClientFactory.X.loadHttpClient(httpClientName);
            options.forEach(httpClient::addHttpOption);
            if (httpClient instanceof BaseHttpClient) {
                BaseHttpClient baseHttpClient = (BaseHttpClient) httpClient;
                interceptors.forEach(baseHttpClient::addInterceptor);
            }
            if (httpClient instanceof HttpOptionSupport) {
                HttpOptionSupport httpOptionSupport = (HttpOptionSupport) httpClient;
                availableOptions.forEach(httpOptionSupport::addMinimumHttpOption);
                minimumOptions.forEach(httpOptionSupport::addAvailableHttpOption);
            }
            return httpClient;
        }
    }

    public interface SolpicTemplateBuilder {

        SolpicTemplateBuilder codec(Codec codec);

        SolpicTemplateBuilder httpClient(HttpClient httpClient);

        SolpicTemplate build();
    }

    private static class DefaultSolpicTemplateBuilder implements SolpicTemplateBuilder {

        private Codec codec;

        private HttpClient httpClient;

        @Override
        public SolpicTemplateBuilder codec(Codec codec) {
            this.codec = codec;
            return this;
        }

        @Override
        public SolpicTemplateBuilder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public SolpicTemplate build() {
            ArgumentUtils.X.notNull("code", codec);
            ArgumentUtils.X.notNull("httpClient", httpClient);
            return new DefaultSolpicTemplate(this.codec, this.httpClient);
        }
    }

    private static class DefaultSolpicTemplate implements SolpicTemplate {

        private final Codec codec;

        private final HttpClient httpClient;

        private DefaultSolpicTemplate(Codec codec, HttpClient httpClient) {
            this.codec = codec;
            this.httpClient = httpClient;
        }

        @Override
        public <S, T> Codec<S, T> getCodec() {
            return (Codec<S, T>) codec;
        }

        @Override
        public HttpClient getHttpClient() {
            return httpClient;
        }

        @Override
        public <S, T> HttpResponse<T> exchange(String requestUrl,
                                               HttpMethod requestMethod,
                                               ContentType requestContentType,
                                               List<HttpHeader> requestHeaders,
                                               S requestPayload,
                                               Function<S, PayloadPublisher> requestPayloadFunction,
                                               PayloadSubscriber<T> responsePayloadSubscriber) {
            HttpClient httpClientToUse = getHttpClient();
            PayloadPublisher requestPayloadPublisher = requestPayloadFunction.apply(requestPayload);
            DefaultHttpRequest request = new DefaultHttpRequest(requestMethod, URI.create(requestUrl), httpClientToUse);
            if (Objects.nonNull(requestContentType)) {
                request.setContentType(requestContentType);
            }
            if (Objects.nonNull(requestHeaders)) {
                requestHeaders.forEach(request::addHeader);
            }
            request.setPayloadPublisher(requestPayloadPublisher);
            return ReadOnlyHttpResponse.of(httpClientToUse.send(request, responsePayloadSubscriber));
        }
    }

    public interface OneWaySolpicTemplateBuilder {

        OneWaySolpicTemplateBuilder codec(Codec codec);

        OneWaySolpicTemplateBuilder httpClient(HttpClient httpClient);

        OneWaySolpicTemplate build();
    }

    private static class DefaultOneWaySolpicTemplateBuilder implements OneWaySolpicTemplateBuilder {

        private Codec codec;

        private HttpClient httpClient;

        @Override
        public OneWaySolpicTemplateBuilder codec(Codec codec) {
            this.codec = codec;
            return this;
        }

        @Override
        public OneWaySolpicTemplateBuilder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public OneWaySolpicTemplate build() {
            ArgumentUtils.X.notNull("code", codec);
            ArgumentUtils.X.notNull("httpClient", httpClient);
            return new DefaultOneWaySolpicTemplate(this.codec, this.httpClient);
        }
    }

    private static class DefaultOneWaySolpicTemplate implements OneWaySolpicTemplate {

        private final Codec codec;

        private final HttpClient httpClient;

        private DefaultOneWaySolpicTemplate(Codec codec, HttpClient httpClient) {
            this.codec = codec;
            this.httpClient = httpClient;
        }

        @Override
        public <S, T> Codec<S, T> getCodec() {
            return codec;
        }

        @Override
        public HttpClient getHttpClient() {
            return httpClient;
        }

        @Override
        public <S> void exchange(String requestUrl,
                                 HttpMethod requestMethod,
                                 ContentType requestContentType,
                                 List<HttpHeader> requestHeaders,
                                 S requestPayload,
                                 Function<S, PayloadPublisher> requestPayloadFunction) {
            HttpClient httpClientToUse = getHttpClient();
            PayloadPublisher requestPayloadPublisher = requestPayloadFunction.apply(requestPayload);
            DefaultHttpRequest request = new DefaultHttpRequest(requestMethod, URI.create(requestUrl), httpClientToUse);
            if (Objects.nonNull(requestContentType)) {
                request.setContentType(requestContentType);
            }
            if (Objects.nonNull(requestHeaders)) {
                requestHeaders.forEach(request::addHeader);
            }
            request.setPayloadPublisher(requestPayloadPublisher);
            httpClientToUse.send(request, PayloadSubscribers.X.discarding());
        }
    }

    static {
        new SolpicShutdownHook().addToShutdownHook();
    }
}
