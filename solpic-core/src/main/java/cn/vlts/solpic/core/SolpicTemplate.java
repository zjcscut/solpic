package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The solpic template.
 *
 * @author throwable
 * @since 2024/7/28 23:28
 */
@SuppressWarnings("unchecked")
public interface SolpicTemplate {

    // ##################### GET METHOD #####################

    default <T> T getForObject(String url, Type responsePayloadType) {
        return getForObject(url, Collections.emptyList(), responsePayloadType);
    }

    default <T> T getForObject(String url, List<HttpHeader> requestHeaders, Type responsePayloadType) {
        return (T) exchange(url, HttpMethod.GET, null, requestHeaders, null,
                responsePayloadType).getPayload();
    }

    default <T> HttpResponse<T> get(String url, Type responsePayloadType) {
        return get(url, Collections.emptyList(), responsePayloadType);
    }

    default <T> HttpResponse<T> get(String url, List<HttpHeader> requestHeaders, Type responsePayloadType) {
        return exchange(url, HttpMethod.GET, null, requestHeaders, null,
                responsePayloadType);
    }

    // ##################### HEAD METHOD #####################

    default List<HttpHeader> headForHeaders(String url) {
        return headForHeaders(url, Collections.emptyList());
    }

    default List<HttpHeader> headForHeaders(String url, List<HttpHeader> requestHeaders) {
        HttpResponse<?> httpResponse = exchange(url, HttpMethod.HEAD, null, requestHeaders, null,
                Void.class);
        return httpResponse.getAllHeaders();
    }

    default HttpResponse<?> head(String url) {
        return head(url, Collections.emptyList());
    }

    default HttpResponse<?> head(String url, List<HttpHeader> requestHeaders) {
        return exchange(url, HttpMethod.HEAD, null, requestHeaders, null, Void.class);
    }

    // ##################### OPTIONS METHOD #####################

    default HttpResponse<?> options(String url) {
        return options(url, Collections.emptyList());
    }

    default HttpResponse<?> options(String url, List<HttpHeader> requestHeaders) {
        return exchange(url, HttpMethod.OPTIONS, null, requestHeaders,
                null, Void.class);
    }

    // ##################### DELETE METHOD #####################

    default HttpResponse<?> delete(String url) {
        return delete(url, Collections.emptyList());
    }

    default HttpResponse<?> delete(String url, List<HttpHeader> requestHeaders) {
        return exchange(url, HttpMethod.DELETE, null, requestHeaders, null, Void.class);
    }

    // ##################### PUT METHOD #####################

    default <S, T> HttpResponse<T> put(String url,
                                       S requestPayload,
                                       Type responsePayloadType) {
        return exchange(url, HttpMethod.PUT, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload,
                responsePayloadType);
    }

    default <S, T> HttpResponse<T> put(String url,
                                       List<HttpHeader> requestHeaders,
                                       ContentType requestContentType,
                                       S requestPayload,
                                       Type responsePayloadType) {
        return exchange(url, HttpMethod.PUT, requestContentType, requestHeaders, requestPayload, responsePayloadType);
    }

    // ##################### POST METHOD #####################

    default <S, T> HttpResponse<T> post(String url,
                                        S requestPayload,
                                        Type responsePayloadType) {
        return exchange(url, HttpMethod.POST, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload,
                responsePayloadType);
    }

    default <S, T> HttpResponse<T> post(String url,
                                        List<HttpHeader> requestHeaders,
                                        ContentType requestContentType,
                                        S requestPayload,
                                        Type responsePayloadType) {
        return exchange(url, HttpMethod.POST, requestContentType, requestHeaders, requestPayload, responsePayloadType);
    }

    // ##################### PATCH METHOD #####################

    default <S, T> HttpResponse<T> patch(String url,
                                         S requestPayload,
                                         Type responsePayloadType) {
        return exchange(url, HttpMethod.PATCH, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload,
                responsePayloadType);
    }

    default <S, T> HttpResponse<T> patch(String url,
                                         List<HttpHeader> requestHeaders,
                                         ContentType requestContentType,
                                         S requestPayload,
                                         Type responsePayloadType) {
        return exchange(url, HttpMethod.PATCH, requestContentType, requestHeaders, requestPayload, responsePayloadType);
    }

    // ##################### BASE METHOD #####################

    <S, T> Codec<S, T> getCodec();

    HttpClient getHttpClient();

    default <S, T> HttpResponse<T> exchange(String requestUrl,
                                            HttpMethod requestMethod,
                                            ContentType requestContentType,
                                            List<HttpHeader> requestHeaders,
                                            S requestPayload,
                                            Type responsePayloadType) {
        PayloadSubscriber<T> payloadSubscriber = PayloadSubscribers.X.getBuildInPayloadSubscriber(responsePayloadType);
        if (Objects.isNull(payloadSubscriber)) {
            payloadSubscriber = (PayloadSubscriber<T>) getCodec().createPayloadSubscriber(responsePayloadType);
        }
        Function<S, PayloadPublisher> requestPayloadFunction;
        if (Objects.isNull(requestPayload)) {
            requestPayloadFunction = sp -> PayloadPublishers.DEFAULT.discarding();
        } else {
            requestPayloadFunction = sp -> getCodec().createPayloadPublisher(sp);
        }
        return exchange(requestUrl, requestMethod, requestContentType, requestHeaders, requestPayload,
                requestPayloadFunction, payloadSubscriber);
    }

    <S, T> HttpResponse<T> exchange(String requestUrl,
                                    HttpMethod requestMethod,
                                    ContentType requestContentType,
                                    List<HttpHeader> requestHeaders,
                                    S requestPayload,
                                    Function<S, PayloadPublisher> requestPayloadFunction,
                                    PayloadSubscriber<T> responsePayloadSubscriber);
}
