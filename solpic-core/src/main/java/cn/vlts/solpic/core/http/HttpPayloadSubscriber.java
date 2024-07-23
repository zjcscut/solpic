package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.flow.Subscriber;

import java.util.concurrent.CompletionStage;

/**
 * HTTP payload subscriber.
 *
 * @author throwable
 * @since 2024/7/24 00:09
 */
public interface HttpPayloadSubscriber<T> extends Subscriber<byte[]> {

    CompletionStage<T> getBody();
}
