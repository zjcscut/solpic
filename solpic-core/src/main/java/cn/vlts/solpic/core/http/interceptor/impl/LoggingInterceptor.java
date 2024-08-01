package cn.vlts.solpic.core.http.interceptor.impl;

import cn.vlts.solpic.core.common.HttpRequestStatus;
import cn.vlts.solpic.core.common.SolpicConstants;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.interceptor.HttpInterceptor;
import cn.vlts.solpic.core.logging.Logger;
import cn.vlts.solpic.core.logging.LoggerFactory;
import cn.vlts.solpic.core.util.Ordered;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Logging HTTP interceptor.
 *
 * @author throwable
 * @since 2024/7/28 13:39
 */
public class LoggingInterceptor implements HttpInterceptor, Ordered {

    private final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public void afterCompletion(HttpRequest request, HttpResponse<?> response) {
        if (request.supportHttpOption(HttpOptions.HTTP_ENABLE_LOGGING)) {
            if (!Objects.equals(HttpRequestStatus.FINISHED, request.getStatus())) {
                return;
            }
            StringBuilder template = new StringBuilder("Finish Executing HTTP request, request URL: %s, response status: %d");
            boolean supportExecuteProfile = false;
            if (request.supportHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE)) {
                template.append(", cost: %d ms");
                supportExecuteProfile = true;
            }
            if (supportExecuteProfile) {
                long costNanos = request.getAttachment(SolpicConstants.REQUEST_COST_NANOS_KEY);
                logger.info(String.format(template.toString(), request.getRawUri(), response.getStatusCode().value(),
                        TimeUnit.NANOSECONDS.toMillis(costNanos)));
            } else {
                logger.info(String.format(template.toString(), request.getRawUri(), response.getStatusCode().value()));
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
