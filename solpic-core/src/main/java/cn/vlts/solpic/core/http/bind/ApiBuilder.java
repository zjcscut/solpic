package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.HttpClient;

/**
 * Api builder.
 *
 * @author throwable
 * @since 2024/8/14 23:55
 */
public interface ApiBuilder {

    ApiBuilder baseUrl(String baseUrl);

    ApiBuilder loadEagerly();

    ApiBuilder httpClient(HttpClient httpClient);

    <T> T build(Class<T> type);

    static ApiBuilder newBuilder() {
        return new DefaultApiBuilder();
    }
}
