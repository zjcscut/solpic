package cn.vlts.solpic.core.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * HTTP protocol version.
 *
 * @author throwable
 * @since 2024/7/19 星期五 17:37
 */
@RequiredArgsConstructor
@Getter
public enum HttpVersion {

    HTTP_0_9("HTTP", 0, 9),

    HTTP_1("HTTP", 1, 0),

    HTTP_1_1("HTTP", 1, 1),

    HTTP_2("HTTP", 2, 0),

    ;

    private final String protocol;

    private final int major;

    private final int minor;

    public static HttpVersion defaultVersion() {
        return HTTP_1_1;
    }
}
