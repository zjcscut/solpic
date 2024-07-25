package cn.vlts.solpic.core.http;

/**
 * Payload support.
 *
 * @author throwable
 * @since 2024/7/26 01:06
 */
public interface PayloadSupport {

    Type getType();

    enum Type {

        BYTES,

        IO_STREAM
    }
}
