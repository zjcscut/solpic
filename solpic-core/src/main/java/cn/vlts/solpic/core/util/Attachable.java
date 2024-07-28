package cn.vlts.solpic.core.util;

import java.util.Map;
import java.util.Set;

/**
 * Attachable.
 *
 * @author throwable
 * @since 2024/7/28 14:17
 */
public interface Attachable {

    Map<AttachmentKey, Object> getAttachments();

    Set<AttachmentKey> getAttachmentKeys();

    <T> void addAttachment(AttachmentKey key, T value);

    <T> void setAttachment(AttachmentKey key, T value);

    <T> T getAttachment(AttachmentKey key);

    <T> T getAttachment(AttachmentKey key, T defaultValue);
}
