package cn.vlts.solpic.core.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * URI scheme.
 *
 * @author throwable
 * @since 2024/7/23 星期二 19:58
 */
@RequiredArgsConstructor
@Getter
public enum UriScheme {

    HTTP("http"),

    HTTPS("https");

    private final String value;

    public boolean isSameAs(String scheme) {
        return value.equalsIgnoreCase(scheme);
    }
}
