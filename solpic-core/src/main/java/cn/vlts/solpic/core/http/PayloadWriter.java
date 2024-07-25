package cn.vlts.solpic.core.http;

/**
 * Payload writer.
 *
 * @author throwable
 * @since 2024/7/26 01:07
 */
public interface PayloadWriter extends PayloadSupport {

    long contentLength();
}
