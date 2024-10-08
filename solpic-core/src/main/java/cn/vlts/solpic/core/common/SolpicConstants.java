package cn.vlts.solpic.core.common;

import cn.vlts.solpic.core.util.AttachmentKey;

/**
 * Solpic constants.
 *
 * @author throwable
 * @since 2024/7/28 13:48
 */
public final class SolpicConstants {

    private SolpicConstants() {
    }

    // ##################### ATTACHMENT KEY #####################

    public static final AttachmentKey REQUEST_START_NANOS_KEY = AttachmentKey.ofKey("startNanos");

    public static final AttachmentKey REQUEST_END_NANOS_KEY = AttachmentKey.ofKey("endNanos");

    public static final AttachmentKey REQUEST_COST_NANOS_KEY = AttachmentKey.ofKey("costNanos");

    public static final AttachmentKey REQUEST_TRACE_ID_KEY = AttachmentKey.ofKey("traceId");
}
