package cn.vlts.solpic.core.http;

import java.util.concurrent.CompletionStage;

/**
 * Payload reader.
 *
 * @author throwable
 * @since 2024/7/26 01:07
 */
public interface PayloadReader<T> extends PayloadSupport {

    long contentLength();

    CompletionStage<T> getBody();
}
