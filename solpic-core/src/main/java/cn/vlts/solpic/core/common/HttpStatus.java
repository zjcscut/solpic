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

    CONTINUE(100, HttpStatusSeries.INFORMATIONAL, "Continue"),

    OK(200, HttpStatusSeries.SUCCESSFUL, "OK"),

    CREATED(201, HttpStatusSeries.SUCCESSFUL, "Created"),

    ACCEPTED(202, HttpStatusSeries.SUCCESSFUL, "Accepted"),

    NON_AUTHORITATIVE_INFORMATION(203, HttpStatusSeries.SUCCESSFUL, "Non Authoritative Information"),

    NO_CONTENT(204, HttpStatusSeries.SUCCESSFUL, "No Content"),

    RESET_CONTENT(205, HttpStatusSeries.SUCCESSFUL, "Reset Content"),

    MULTIPLE_CHOICES(300, HttpStatusSeries.REDIRECTION, "Multiple Choices"),

    MOVED_PERMANENTLY(301, HttpStatusSeries.REDIRECTION, "Moved Permanently"),

    MOVED_TEMPORARILY(302, HttpStatusSeries.REDIRECTION, "Moved Temporarily"),

    SEE_OTHER(303, HttpStatusSeries.REDIRECTION, "See Other"),

    NOT_MODIFIED(304, HttpStatusSeries.REDIRECTION, "Not Modified"),

    USE_PROXY(305, HttpStatusSeries.REDIRECTION, "Use Proxy"),

    TEMPORARY_REDIRECT(307, HttpStatusSeries.REDIRECTION, "Temporary Redirect"),

    PERMANENT_REDIRECT(308, HttpStatusSeries.REDIRECTION, "Permanent Redirect"),

    BAD_REQUEST(400, HttpStatusSeries.CLIENT_ERROR, "Bad Request"),

    UNAUTHORIZED(401, HttpStatusSeries.CLIENT_ERROR, "Unauthorized"),

    PAYMENT_REQUIRED(402, HttpStatusSeries.CLIENT_ERROR, "Payment Required"),

    FORBIDDEN(403, HttpStatusSeries.CLIENT_ERROR, "Forbidden"),

    NOT_FOUND(404, HttpStatusSeries.CLIENT_ERROR, "Not Found"),

    METHOD_NOT_ALLOWED(405, HttpStatusSeries.CLIENT_ERROR, "Method Not Allowed"),

    INTERNAL_SERVER_ERROR(500, HttpStatusSeries.SERVER_ERROR, "Internal Server Error"),

    NOT_IMPLEMENTED(501, HttpStatusSeries.SERVER_ERROR, "Not Implemented"),

    BAD_GATEWAY(502, HttpStatusSeries.SERVER_ERROR, "Bad Gateway"),

    SERVICE_UNAVAILABLE(503, HttpStatusSeries.SERVER_ERROR, "Service Unavailable"),

    GATEWAY_TIMEOUT(504, HttpStatusSeries.SERVER_ERROR, "Gateway Timeout"),

    HTTP_VERSION_NOT_SUPPORTED(505, HttpStatusSeries.SERVER_ERROR, "HTTP Version Not Supported"),

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
