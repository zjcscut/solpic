package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.common.PayloadSupportType;

/**
 * Payload support.
 *
 * @author throwable
 * @since 2024/7/26 01:06
 */
public interface PayloadSupport {

    long getContentLength();

    PayloadSupportType getType();
}
