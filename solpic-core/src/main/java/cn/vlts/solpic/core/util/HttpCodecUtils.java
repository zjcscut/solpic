package cn.vlts.solpic.core.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * HTTP codec utils.
 *
 * @author throwable
 * @since 2024/8/9 星期五 15:21
 */
public enum HttpCodecUtils {
    X;

    public String encodeValue(String value, Charset charset) {
        try {
            return URLEncoder.encode(value, charset.name());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String decodeValue(String value, Charset charset) {
        try {
            return URLDecoder.decode(value, charset.name());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
