package cn.vlts.solpic.core.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

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
        return Objects.nonNull(scheme) && value.equalsIgnoreCase(scheme);
    }
}
