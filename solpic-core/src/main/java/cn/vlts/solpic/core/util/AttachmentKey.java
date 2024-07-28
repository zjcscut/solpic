package cn.vlts.solpic.core.util;

/**
 * Attachment key.
 *
 * @author throwable
 * @since 2024/7/28 14:01
 */
public interface AttachmentKey {

    int id();

    String key();

    static AttachmentKey ofKey(String key) {
        return new DefaultAttachmentKey(key);
    }

    static AttachmentKey of() {
        return new DefaultAttachmentKey();
    }
}
