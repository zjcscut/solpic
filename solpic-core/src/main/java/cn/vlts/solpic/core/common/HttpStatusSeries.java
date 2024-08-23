package cn.vlts.solpic.core.common;

import lombok.RequiredArgsConstructor;

/**
 * HTTP status series.
 *
 * @author throwable
 * @since 2024/7/23 23:31
 */
@RequiredArgsConstructor
public enum HttpStatusSeries {

    UNKNOWN((byte) 0),

    INFORMATIONAL((byte) 1),

    SUCCESSFUL((byte) 2),

    REDIRECTION((byte) 3),

    CLIENT_ERROR((byte) 4),

    SERVER_ERROR((byte) 5),

    ;

    private final byte value;

    public byte value() {
        return this.value;
    }

    public static HttpStatusSeries fromStatusCode(int statusCode) {
        byte seriesCode = (byte) (statusCode / 100);
        for (HttpStatusSeries series : HttpStatusSeries.values()) {
            if (series.value() == seriesCode) {
                return series;
            }
        }
        return UNKNOWN;
    }
}
