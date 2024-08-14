package cn.vlts.solpic.core.http.interceptor.impl;

import cn.vlts.solpic.core.common.SolpicConstants;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.interceptor.HttpInterceptor;
import cn.vlts.solpic.core.util.Ordered;

import java.util.Objects;

/**
 * Profile execution HTTP interceptor.
 *
 * @author throwable
 * @since 2024/7/28 13:39
 */
public class ProfileExecutionInterceptor implements HttpInterceptor, Ordered {

    @Override
    public void beforeSend(HttpRequest request) {
        if (Objects.equals(Boolean.TRUE, request.getHttpOptionValue(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE))) {
            request.setAttachment(SolpicConstants.REQUEST_START_NANOS_KEY, System.nanoTime());
        }
    }

    @Override
    public void afterCompletion(HttpRequest request, HttpResponse<?> response) {
        if (Objects.equals(Boolean.TRUE, request.getHttpOptionValue(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE))) {
            long requestEndNanos = System.nanoTime();
            request.setAttachment(SolpicConstants.REQUEST_END_NANOS_KEY, requestEndNanos);
            long requestStartNanos = request.getAttachment(SolpicConstants.REQUEST_START_NANOS_KEY);
            long requestCostNanos = requestEndNanos - requestStartNanos;
            request.setAttachment(SolpicConstants.REQUEST_COST_NANOS_KEY, requestCostNanos);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
