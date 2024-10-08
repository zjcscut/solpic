package cn.vlts.solpic.core.http.interceptor.impl;

import cn.vlts.solpic.core.common.SolpicConstants;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.interceptor.HttpInterceptor;
import cn.vlts.solpic.core.util.FastUUIDUtils;
import cn.vlts.solpic.core.util.Ordered;

import java.util.Objects;

/**
 * Tracing HTTP interceptor.
 *
 * @author throwable
 * @since 2024/7/28 23:49
 */
public class TracingInterceptor implements HttpInterceptor, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void beforeSend(HttpRequest request) {
        if (Objects.equals(Boolean.TRUE, request.getHttpOptionValue(HttpOptions.HTTP_ENABLE_EXECUTE_TRACING))) {
            request.setAttachment(SolpicConstants.REQUEST_TRACE_ID_KEY, FastUUIDUtils.X.newRandomUUID());
        }
    }
}
