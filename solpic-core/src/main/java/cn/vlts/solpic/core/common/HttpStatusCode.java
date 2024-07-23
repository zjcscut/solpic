package cn.vlts.solpic.core.common;

import java.util.Objects;

/**
 * HTTP status code.
 *
 * @author throwable
 * @since 2024/7/23 23:14
 */
public interface HttpStatusCode {

    int value();

    boolean isInformational();

    boolean isSuccessful();

    boolean isRedirection();

    boolean isClientError();

    boolean isServerError();

    boolean isError();

    default boolean isSameAs(HttpStatusCode other) {
        return this.value() == other.value();
    }

    default HttpStatusCode fromStatusCode(int statusCode) {
        if (statusCode < 100 || statusCode > 999) {
            throw new IllegalArgumentException("Invalid status code: " + statusCode);
        }
        HttpStatus httpStatus = HttpStatus.ofStatusCode(statusCode);
        return Objects.nonNull(httpStatus) ? httpStatus : new DefaultHttpStatusCode(statusCode);
    }
}
