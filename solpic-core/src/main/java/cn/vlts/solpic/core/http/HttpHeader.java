package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.http.impl.BasicHttpHeader;
import cn.vlts.solpic.core.util.Pair;

/**
 * HTTP header.
 *
 * @author throwable
 * @since 2024/7/19 星期五 16:13
 */
public interface HttpHeader extends Pair {

    boolean isSensitive();

    static HttpHeader of(String name, String value) {
        return BasicHttpHeader.of(name, value);
    }
}
