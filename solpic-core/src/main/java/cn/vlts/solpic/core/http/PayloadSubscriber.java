package cn.vlts.solpic.core.http;

import java.util.concurrent.CompletionStage;

/**
 * Payload subscriber.
 *
 * @author throwable
 * @since 2024/7/26 01:07
 */
public interface PayloadSubscriber<T> extends PayloadSupport {

    CompletionStage<T> getPayload();
}
