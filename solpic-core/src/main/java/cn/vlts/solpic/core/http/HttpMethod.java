package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.util.CaseInsensitiveMap;
import cn.vlts.solpic.core.util.Cis;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * HTTP method.
 *
 * @author throwable
 * @since 2024/7/23 星期二 19:43
 */
public enum HttpMethod {

    OPTIONS,

    HEAD,

    GET,

    POST,

    PUT,

    DELETE,

    PATCH,

    TRACE,

    ;

    private static final CaseInsensitiveMap<Cis, HttpMethod> CACHE = new CaseInsensitiveMap<>(8);

    static {
        Stream.of(HttpMethod.values()).forEach(item -> CACHE.putIfAbsent0(item.name(), item));
    }

    public static HttpMethod fromMethod(String method) {
        return Optional.ofNullable(CACHE.get0(method)).orElseThrow(() -> new IllegalArgumentException("Invalid HTTP method: " + method));
    }
}
