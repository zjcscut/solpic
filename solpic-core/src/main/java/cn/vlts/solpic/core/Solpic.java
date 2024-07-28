package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * The solpic.
 *
 * @author throwable
 * @since 2024/7/28 23:27
 */
public abstract class Solpic {

    private static class DefaultSolpicTemplate implements SolpicTemplate {

        private Codec codec;

        private HttpClient httpClient;

        @SuppressWarnings("unchecked")
        @Override
        public <S, T> Codec<S, T> getCodec() {
            return (Codec<S, T>) codec;
        }

        @Override
        public HttpClient getHttpClient() {
            return httpClient;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public <S, T> T exchange(String requestUrl,
                                 HttpMethod requestMethod,
                                 ContentType requestContentType,
                                 Map<String, String> requestHeaders,
                                 S requestPayload,
                                 Type responsePayloadType) {
            PayloadSubscriber<T> payloadSubscriber;
            if (Objects.equals(Void.class, responsePayloadType)) {
                payloadSubscriber = PayloadSubscribers.DEFAULT.discarding();
            } else {
                payloadSubscriber = (PayloadSubscriber<T>) getCodec().createPayloadSubscriber(responsePayloadType);
            }
            PayloadPublisher payloadPublisher;
            if (Objects.isNull(requestPayload)) {
                payloadPublisher = PayloadPublishers.DEFAULT.discarding();
            } else {
                payloadPublisher = getCodec().createPayloadPublisher(requestPayload);
            }
            DefaultHttpRequest request = new DefaultHttpRequest(requestMethod, URI.create(requestUrl));
            if (Objects.nonNull(requestHeaders)) {
                requestHeaders.forEach(request::addHeader);
            }
            if (Objects.nonNull(requestContentType)) {
                request.setContentType(requestContentType);
            }
            return getHttpClient().send(request, payloadPublisher, payloadSubscriber).getPayload();
        }
    }
}
