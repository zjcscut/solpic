package cn.vlts.solpic.core.http;

/**
 * Request payload support.
 *
 * @author throwable
 * @since 2024/8/2 00:24
 */
public interface RequestPayloadSupport {

    long contentLength();

    default ContentType contentType() {
        return null;
    }
}
