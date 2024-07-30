package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * One-Way solpic template.
 *
 * @author throwable
 * @since 2024/7/29 星期一 9:57
 */
public interface OneWaySolpicTemplate {

    // ##################### GET METHOD #####################

    default void get(String url, Type responsePayloadType) {
        get(url, Collections.emptyList(), responsePayloadType);
    }

    default void get(String url, List<HttpHeader> requestHeaders, Type responsePayloadType) {
        exchange(url, HttpMethod.GET, null, requestHeaders, null);
    }

    // ##################### HEAD METHOD #####################

    default void head(String url) {
        head(url, Collections.emptyList());
    }

    default void head(String url, List<HttpHeader> requestHeaders) {
        exchange(url, HttpMethod.HEAD, null, requestHeaders, null);
    }

    // ##################### OPTIONS METHOD #####################

    default void options(String url) {
        options(url, Collections.emptyList());
    }

    default void options(String url, List<HttpHeader> requestHeaders) {
        exchange(url, HttpMethod.OPTIONS, null, requestHeaders, null);
    }

    // ##################### DELETE METHOD #####################

    default void delete(String url) {
        delete(url, Collections.emptyList());
    }

    default void delete(String url, List<HttpHeader> requestHeaders) {
        exchange(url, HttpMethod.DELETE, null, requestHeaders, null);
    }

    // ##################### PUT METHOD #####################

    default <S> void put(String url, S requestPayload) {
        exchange(url, HttpMethod.PUT, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload);
    }

    default <S> void put(String url,
                         List<HttpHeader> requestHeaders,
                         ContentType requestContentType,
                         S requestPayload) {
        exchange(url, HttpMethod.PUT, requestContentType, requestHeaders, requestPayload);
    }

    // ##################### POST METHOD #####################

    default <S> void post(String url, S requestPayload, Type responsePayloadType) {
        exchange(url, HttpMethod.POST, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload);
    }

    default <S> void post(String url,
                          List<HttpHeader> requestHeaders,
                          ContentType requestContentType,
                          S requestPayload) {
        exchange(url, HttpMethod.POST, requestContentType, requestHeaders, requestPayload);
    }

    // ##################### PATCH METHOD #####################

    default <S> void patch(String url, S requestPayload) {
        exchange(url, HttpMethod.PATCH, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload);
    }

    default <S> void patch(String url,
                           List<HttpHeader> requestHeaders,
                           ContentType requestContentType,
                           S requestPayload) {
        exchange(url, HttpMethod.PATCH, requestContentType, requestHeaders, requestPayload);
    }

    // ##################### BASE METHOD #####################

    <S, T> Codec<S, T> getCodec();

    HttpClient getHttpClient();

    default <S> void exchange(String requestUrl,
                              HttpMethod requestMethod,
                              ContentType requestContentType,
                              List<HttpHeader> requestHeaders,
                              S requestPayload) {
        Function<S, PayloadPublisher> requestPayloadFunction;
        Class<?> requestPayloadClazz;
        if (Objects.isNull(requestPayload)) {
            requestPayloadFunction = sp -> PayloadPublishers.DEFAULT.discarding();
        } else if (PayloadPublishers.X.containsBuildInPayloadPublisher(requestPayloadClazz = requestPayload.getClass())) {
            requestPayloadFunction = PayloadPublishers.X.getBuildInPayloadPublisher(requestPayloadClazz);
        } else {
            requestPayloadFunction = sp -> getCodec().createPayloadPublisher(sp);
        }
        exchange(requestUrl, requestMethod, requestContentType, requestHeaders, requestPayload,
                requestPayloadFunction);
    }

    <S> void exchange(String requestUrl,
                      HttpMethod requestMethod,
                      ContentType requestContentType,
                      List<HttpHeader> requestHeaders,
                      S requestPayload,
                      Function<S, PayloadPublisher> requestPayloadFunction);
}
