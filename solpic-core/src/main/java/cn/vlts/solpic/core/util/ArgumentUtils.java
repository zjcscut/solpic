package cn.vlts.solpic.core.util;

import java.util.Objects;

/**
 * Argument utils.
 *
 * @author throwable
 * @since 2024/8/9 星期五 15:00
 */
public enum ArgumentUtils {
    X;

    public void notNull(String name, Object value) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("Argument '" + name + "' must not be null");
        }
    }

    public void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public boolean hasLength(String string) {
        return string != null && !string.isEmpty();
    }
}
