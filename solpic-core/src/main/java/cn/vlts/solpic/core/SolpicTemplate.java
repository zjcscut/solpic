package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The solpic template.
 *
 * @author throwable
 * @since 2024/7/28 23:28
 */
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

    // ##################### BASE METHOD #####################

    <S, T> Codec<S, T> getCodec();

    HttpClient getHttpClient();

    <S, T> HttpResponse<T> exchange(String requestUrl,
                                    HttpMethod requestMethod,
                                    ContentType requestContentType,
                                    List<HttpHeader> requestHeaders,
                                    S requestPayload,
                                    Type responsePayloadType);
}
