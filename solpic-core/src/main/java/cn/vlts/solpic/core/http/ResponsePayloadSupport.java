package cn.vlts.solpic.core.http;

import java.util.concurrent.CompletionStage;

/**
 * @author throwable
 * @version v1
 * @description
 * @since 2024/8/2 00:24
 */
public interface ResponsePayloadSupport<T> {

    CompletionStage<T> getPayload();
}
