package cn.vlts.solpic.core.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;

/**
 * URI builder.
 *
 * @author throwable
 * @since 2024/7/23 星期二 19:55
 */
public final class UriBuilder {

    public static UriBuilder newInstance(String uri) {
        return new UriBuilder();
    }

    public static UriBuilder newInstance(URI uri) {
        return new UriBuilder();
    }

    public UriBuilder withScheme(String scheme) {
        return this;
    }

    public URI build() {
        return null;
    }

    public static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
