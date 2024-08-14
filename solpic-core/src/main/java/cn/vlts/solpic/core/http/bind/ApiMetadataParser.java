package cn.vlts.solpic.core.http.bind;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Api metadata parser.
 *
 * @author throwable
 * @since 2024/8/14 23:51
 */
public enum ApiMetadataParser {
    X;

    private static final ConcurrentMap<Method, ApiMetadata> API_METADATA_CACHE = new ConcurrentHashMap<>();

    public ApiMetadata parse(DefaultApiBuilder builder, Class<?> type, Method method) {
        return API_METADATA_CACHE.computeIfAbsent(method, m -> {
            ApiMetadata apiMetadata = new ApiMetadata();
            String baseUrl = builder.getBaseUrl();
            return apiMetadata;
        });
    }

    public boolean existApiMetadata(Method method) {
        return API_METADATA_CACHE.containsKey(method);
    }

    public ApiMetadata getApiMetadata(Method method) {
        return API_METADATA_CACHE.get(method);
    }
}
