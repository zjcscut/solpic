package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Flow payload subscriber.
 *
 * @author throwable
 * @since 2024/7/30 星期二 16:42
 */
public interface FlowPayloadSubscriber<T> extends Subscriber<List<ByteBuffer>>, ResponsePayloadSupport<T> {

}
