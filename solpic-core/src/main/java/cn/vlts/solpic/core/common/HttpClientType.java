package cn.vlts.solpic.core.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * HTTP client type.
 *
 * @author throwable
 * @since 2024/8/8 星期四 18:19
 */
@RequiredArgsConstructor
@Getter
public enum HttpClientType {

    /**
     * Default, base on HttpURLConnection.
     */
    DEFAULT("default"),

    /**
     * OkHttp 4.x.
     */
    OKHTTP("okhttp"),

    /**
     * Apache HttpClient 4.x.
     */
    APACHE_HTTPCLIENT_V4("ahc4"),

    /**
     * Apache HttpClient 5.x.
     */
    APACHE_HTTPCLIENT_V5("ahc5"),

    /**
     * JDK11+ HttpClient.
     */
    JDK_HTTPCLIENT("jhc"),

    ;

    private final String code;

    public static HttpClientType fromCode(String otherCode) {
        for (HttpClientType httpClientType : HttpClientType.values()) {
            if (Objects.equals(httpClientType.getCode(), otherCode)) {
                return httpClientType;
            }
        }
        return null;
    }
}
