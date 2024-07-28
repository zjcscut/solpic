package cn.vlts.solpic.core.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Payload publisher.
 *
 * @author throwable
 * @since 2024/7/26 01:07
 */
public interface PayloadPublisher extends PayloadSupport {

    void writeTo(OutputStream outputStream, boolean autoClose) throws IOException;

    default void writeTo(OutputStream outputStream) throws IOException {
        writeTo(outputStream, true);
    }
}
