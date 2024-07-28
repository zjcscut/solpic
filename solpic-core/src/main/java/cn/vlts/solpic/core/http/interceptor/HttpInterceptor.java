package cn.vlts.solpic.core.http.interceptor;

import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.HttpResponse;

/**
 * HTTP interceptor.
 *
 * @author throwable
 * @since 2024/7/28 01:14
 */
public interface HttpInterceptor {

    default void beforeSend(HttpRequest request) {

    }

    default void afterSend(HttpRequest request, HttpResponse<?> response) {

    }

    default void afterCompletion(HttpRequest request, HttpResponse<?> response) {

    }
}
