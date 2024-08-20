package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.bind.MultipartData;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Path;
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

    default <T> T getForObject(String url,
                               List<HttpHeader> requestHeaders,
                               PayloadSubscriber<T> responsePayloadSubscriber) {
        return exchange(url, HttpMethod.GET, null, requestHeaders, null,
                responsePayloadSubscriber).getPayload();
    }

    default <T> HttpResponse<T> get(String url, Type responsePayloadType) {
        return get(url, Collections.emptyList(), responsePayloadType);
    }

    default <T> HttpResponse<T> get(String url, List<HttpHeader> requestHeaders, Type responsePayloadType) {
        return exchange(url, HttpMethod.GET, null, requestHeaders, null,
                responsePayloadType);
    }

    default <T> HttpResponse<T> get(String url,
                                    List<HttpHeader> requestHeaders,
                                    PayloadSubscriber<T> responsePayloadSubscriber) {
        return exchange(url, HttpMethod.GET, null, requestHeaders, null,
                responsePayloadSubscriber);
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

    default <S, T> T postForObject(String url,
                                   S requestPayload,
                                   Type responsePayloadType) {
        return (T) exchange(url, HttpMethod.POST, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload,
                responsePayloadType).getPayload();
    }

    default <S, T> T postForObject(String url,
                                   List<HttpHeader> requestHeaders,
                                   ContentType requestContentType,
                                   S requestPayload,
                                   Type responsePayloadType) {
        return (T) exchange(url, HttpMethod.POST, requestContentType, requestHeaders, requestPayload,
                responsePayloadType).getPayload();
    }

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

    default <S, T> T patchForObject(String url,
                                    S requestPayload,
                                    Type responsePayloadType) {
        return (T) exchange(url, HttpMethod.PATCH, ContentType.APPLICATION_JSON, Collections.emptyList(), requestPayload,
                responsePayloadType).getPayload();
    }

    default <S, T> T patchForObject(String url,
                                    List<HttpHeader> requestHeaders,
                                    ContentType requestContentType,
                                    S requestPayload,
                                    Type responsePayloadType) {
        return (T) exchange(url, HttpMethod.PATCH, requestContentType, requestHeaders, requestPayload, responsePayloadType)
                .getPayload();
    }

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

    // ##################### ADVANCED METHOD #####################

    default <S> HttpResponse<Void> download(String url, Path targetFile) {
        return exchange(url, HttpMethod.GET, null, null, null,
                PayloadSubscribers.X.ofFile(targetFile));
    }

    default <S> HttpResponse<byte[]> download(String url) {
        return exchange(url, HttpMethod.GET, null, null, null,
                PayloadSubscribers.X.ofByteArray());
    }

    default <S> HttpResponse<Void> download(String url,
                                            HttpMethod requestMethod,
                                            List<HttpHeader> requestHeaders,
                                            ContentType requestContentType,
                                            Path targetFile,
                                            Charset charset,
                                            S requestPayload) {
        return exchange(url, requestMethod, requestContentType, requestHeaders, requestPayload,
                PayloadSubscribers.X.ofFile(targetFile, charset));
    }

    default <S> HttpResponse<Void> upload(String url, String name, Path sourceFile) {
        MultipartData multipartData = MultipartData.newBuilder().addFilePart(name, sourceFile).build();
        return exchange(url, HttpMethod.PUT, null, null, multipartData, Void.class);
    }

    default <T> HttpResponse<T> upload(String url, String name, Path sourceFile, Type responsePayloadType) {
        MultipartData multipartData = MultipartData.newBuilder().addFilePart(name, sourceFile).build();
        return exchange(url, HttpMethod.PUT, null, null, multipartData, responsePayloadType);
    }

    default <T> HttpResponse<T> upload(String url,
                                       String name,
                                       Path sourceFile,
                                       Charset charset,
                                       List<HttpHeader> requestHeaders,
                                       ContentType partContentType,
                                       Type responsePayloadType) {
        MultipartData multipartData = MultipartData.newBuilder(charset)
                .addFilePart(name, sourceFile, partContentType)
                .build();
        return exchange(url, HttpMethod.PUT, null, requestHeaders, multipartData, responsePayloadType);
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
        PayloadSubscriber<T> responePayloadSubscriber = Objects.nonNull(responsePayloadType) ?
                PayloadSubscribers.X.getPayloadSubscriber(responsePayloadType) : PayloadSubscribers.X.discarding();
        if (Objects.isNull(responePayloadSubscriber) &&
                ReflectionUtils.X.isAssignableFrom(responsePayloadType, PayloadSubscriber.class)) {
            responePayloadSubscriber = ReflectionUtils.X.createInstance(
                    (Class<? extends PayloadSubscriber<T>>) responsePayloadType);
        }
        if (Objects.isNull(responePayloadSubscriber)) {
            responePayloadSubscriber = (PayloadSubscriber<T>) getCodec().createPayloadSubscriber(responsePayloadType);
        }
        return exchange(requestUrl, requestMethod, requestContentType, requestHeaders, requestPayload,
                responePayloadSubscriber);
    }

    default <S, T> HttpResponse<T> exchange(String requestUrl,
                                            HttpMethod requestMethod,
                                            ContentType requestContentType,
                                            List<HttpHeader> requestHeaders,
                                            S requestPayload,
                                            PayloadSubscriber<T> responsePayloadSubscriber) {
        Function<S, PayloadPublisher> requestPayloadFunction;
        Class<?> requestPayloadClazz;
        if (Objects.isNull(requestPayload)) {
            requestPayloadFunction = sp -> PayloadPublishers.X.discarding();
        } else if (requestPayload instanceof PayloadPublisher) {
            requestPayloadFunction = sp -> (PayloadPublisher) requestPayload;
        } else if (PayloadPublishers.X.containsPayloadPublisher(requestPayloadClazz = requestPayload.getClass())) {
            requestPayloadFunction = PayloadPublishers.X.getPayloadPublisher(requestPayloadClazz);
        } else {
            requestPayloadFunction = sp -> getCodec().createPayloadPublisher(sp);
        }
        return exchange(requestUrl, requestMethod, requestContentType, requestHeaders, requestPayload,
                requestPayloadFunction, responsePayloadSubscriber);
    }

    <S, T> HttpResponse<T> exchange(String requestUrl,
                                    HttpMethod requestMethod,
                                    ContentType requestContentType,
                                    List<HttpHeader> requestHeaders,
                                    S requestPayload,
                                    Function<S, PayloadPublisher> requestPayloadFunction,
                                    PayloadSubscriber<T> responsePayloadSubscriber);
}
