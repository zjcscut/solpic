package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.util.Pair;
import cn.vlts.solpic.core.util.SimpleLRUCache;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Content type.
 *
 * @author throwable
 * @since 2024/7/22 23:01
 */
public final class ContentType {

    private static final SimpleLRUCache<String, ContentType> CACHE = new SimpleLRUCache<>(16);

    private final String mimeType;

    private final Charset charset;

    private final Pair[] params;

    private ContentType(String mimeType,
                        Charset charset) {
        this(mimeType, charset, null);
    }

    private ContentType(String mimeType,
                        Charset charset,
                        Pair[] params) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = params;
    }

    public static ContentType parse(String contentType) {
        if (Objects.isNull(contentType)) {
            return null;
        }
        return CACHE.computeIfAbsent(contentType, ct -> {
            return null;
        });
    }

    @Override
    public String toString() {
        return getValue();
    }

    public String getValue() {
        return null;
    }
}
