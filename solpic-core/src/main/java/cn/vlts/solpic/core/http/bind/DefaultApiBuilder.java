package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.HttpClient;

/**
 * Default api builder.
 *
 * @author throwable
 * @since 2024/8/14 23:54
 */
class DefaultApiBuilder implements ApiBuilder {

    private boolean loadEagerly = false;

    private String baseUrl;

    @Override
    public ApiBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public ApiBuilder loadEagerly() {
        this.loadEagerly = true;
        return this;
    }

    @Override
    public ApiBuilder httpClient(HttpClient httpClient) {
        return null;
    }

    @Override
    public <T> T build(Class<T> type) {
        return null;
    }

    public boolean isLoadEagerly() {
        return loadEagerly;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
