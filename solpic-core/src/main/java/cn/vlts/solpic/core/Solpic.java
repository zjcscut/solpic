package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.http.impl.ReadOnlyHttpResponse;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The solpic.
 *
 * @author throwable
 * @since 2024/7/28 23:27
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Solpic {

    public static SolpicTemplateBuilder newSolpicTemplateBuilder() {
        return new DefaultSolpicTemplateBuilder();
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
                                               Type responsePayloadType) {
            PayloadSubscriber<T> payloadSubscriber = PayloadSubscribers.X.getBuildInPayloadSubscriber(responsePayloadType);
            if (Objects.isNull(payloadSubscriber)) {
                payloadSubscriber = (PayloadSubscriber<T>) getCodec().createPayloadSubscriber(responsePayloadType);
            }
            PayloadPublisher payloadPublisher;
            if (Objects.isNull(requestPayload)) {
                payloadPublisher = PayloadPublishers.DEFAULT.discarding();
            } else {
                payloadPublisher = getCodec().createPayloadPublisher(requestPayload);
            }
            DefaultHttpRequest request = new DefaultHttpRequest(requestMethod, URI.create(requestUrl));
            if (Objects.nonNull(requestContentType)) {
                request.setContentType(requestContentType);
            }
            if (Objects.nonNull(requestHeaders)) {
                requestHeaders.forEach(request::addHeader);
            }
            return ReadOnlyHttpResponse.of(getHttpClient().send(request, payloadPublisher, payloadSubscriber));
        }
    }

    public interface HttpClientBuilder {

    }
}
