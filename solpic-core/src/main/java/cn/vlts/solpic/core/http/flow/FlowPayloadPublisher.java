package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.Publisher;
import cn.vlts.solpic.core.http.RequestPayloadSupport;

import java.nio.ByteBuffer;

/**
 * Flow payload publisher.
 *
 * @author throwable
 * @since 2024/7/30 星期二 17:40
 */
public interface FlowPayloadPublisher extends Publisher<ByteBuffer>, RequestPayloadSupport {

}
