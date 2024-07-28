package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpMethod;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * The solpic template.
 *
 * @author throwable
 * @since 2024/7/28 23:28
 */
public interface SolpicTemplate {

    <S, T> Codec<S, T> getCodec();

    HttpClient getHttpClient();

    <S, T> T exchange(String requestUrl,
                      HttpMethod requestMethod,
                      ContentType requestContentType,
                      Map<String, String> requestHeaders,
                      S requestPayload,
                      Type responsePayloadType);
}
