package cn.vlts.solpic.core.common;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * HTTP status.
 *
 * @author throwable
 * @since 2024/7/23 23:17
 */
@RequiredArgsConstructor
public enum HttpStatus implements HttpStatusCode {

    ;

    private final int value;

    private final HttpStatusSeries series;

    private final String reasonPhrase;

    private static final ConcurrentMap<Integer, HttpStatus> CACHE = new ConcurrentHashMap<>();

    static {
        Stream.of(HttpStatus.values()).forEach(httpStatus -> CACHE.put(httpStatus.value(), httpStatus));
    }

    @Override
    public int value() {
        return this.value;
    }

    public HttpStatusSeries series() {
        return this.series;
    }

    public String reasonPhrase() {
        return this.reasonPhrase;
    }

    @Override
    public boolean isInformational() {
        return this.series == HttpStatusSeries.INFORMATIONAL;
    }

    @Override
    public boolean isSuccessful() {
        return this.series == HttpStatusSeries.SUCCESSFUL;
    }

    @Override
    public boolean isRedirection() {
        return this.series == HttpStatusSeries.REDIRECTION;
    }

    @Override
    public boolean isClientError() {
        return this.series == HttpStatusSeries.CLIENT_ERROR;
    }

    @Override
    public boolean isServerError() {
        return this.series == HttpStatusSeries.SERVER_ERROR;
    }

    @Override
    public boolean isError() {
        return isClientError() || isServerError();
    }

    public static HttpStatus ofStatusCode(int statusCode) {
        return CACHE.get(statusCode);
    }
}
