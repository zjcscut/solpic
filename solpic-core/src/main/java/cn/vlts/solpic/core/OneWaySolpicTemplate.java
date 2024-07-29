package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpMethod;

import java.util.Map;

/**
 * One-Way solpic template.
 *
 * @author throwable
 * @since 2024/7/29 星期一 9:57
 */
public interface OneWaySolpicTemplate {

    <S, T> Codec<S, T> getCodec();

    HttpClient getHttpClient();

    <S> void exchange(String requestUrl,
                      HttpMethod requestMethod,
                      ContentType requestContentType,
                      Map<String, String> requestHeaders,
                      S requestPayload);
}
