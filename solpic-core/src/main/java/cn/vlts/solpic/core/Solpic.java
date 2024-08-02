package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.codec.CodecFactory;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.http.impl.ReadOnlyHttpResponse;

import java.net.URI;
import java.util.List;
import java.util.Objects;
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

    public static SolpicTemplate newSolpicTemplate() {
        return new DefaultSolpicTemplateBuilder()
                .httpClient(null)
                .codec(CodecFactory.X.loadBestMatchedCodec())
                .build();
    }

    public static SolpicTemplateBuilder newSolpicTemplateBuilder() {
        return new DefaultSolpicTemplateBuilder();
    }

    public static OneWaySolpicTemplate newOneWaySolpicTemplate() {
        return new DefaultOneWaySolpicTemplateBuilder()
                .httpClient(null)
                .codec(CodecFactory.X.loadBestMatchedCodec())
                .build();
    }

    public static OneWaySolpicTemplateBuilder newOneWaySolpicTemplateBuilder() {
        return new DefaultOneWaySolpicTemplateBuilder();
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
            if (Objects.isNull(this.codec)) {
                throw new IllegalArgumentException("Codec must not be null");
            }
            if (Objects.isNull(this.httpClient)) {
                throw new IllegalArgumentException("HttpClient must not be null");
            }
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
            PayloadPublisher requestPayloadPublisher = requestPayloadFunction.apply(requestPayload);
            DefaultHttpRequest request = new DefaultHttpRequest(requestMethod, URI.create(requestUrl), getHttpClient());
            if (Objects.nonNull(requestContentType)) {
                request.setContentType(requestContentType);
            }
            if (Objects.nonNull(requestHeaders)) {
                requestHeaders.forEach(request::addHeader);
            }
            return ReadOnlyHttpResponse.of(getHttpClient().send(request, requestPayloadPublisher,
                    responsePayloadSubscriber));
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
            if (Objects.isNull(this.codec)) {
                throw new IllegalArgumentException("Codec must not be null");
            }
            if (Objects.isNull(this.httpClient)) {
                throw new IllegalArgumentException("HttpClient must not be null");
            }
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
            PayloadPublisher requestPayloadPublisher = requestPayloadFunction.apply(requestPayload);
            DefaultHttpRequest request = new DefaultHttpRequest(requestMethod, URI.create(requestUrl), getHttpClient());
            if (Objects.nonNull(requestContentType)) {
                request.setContentType(requestContentType);
            }
            if (Objects.nonNull(requestHeaders)) {
                requestHeaders.forEach(request::addHeader);
            }
            getHttpClient().send(request, requestPayloadPublisher, PayloadSubscribers.DEFAULT.discarding());
        }
    }

    public interface HttpClientBuilder {

    }
}
