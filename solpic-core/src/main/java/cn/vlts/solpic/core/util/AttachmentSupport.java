package cn.vlts.solpic.core.util;

import java.util.*;

/**
 * Attachment support.
 *
 * @author throwable
 * @since 2024/7/28 00:59
 */
public abstract class AttachmentSupport implements Attachable {

    private final Map<AttachmentKey, Object> attachments = new HashMap<>();

    @Override
    public Map<AttachmentKey, Object> getAttachments() {
        return Collections.unmodifiableMap(this.attachments);
    }

    @Override
    public Set<AttachmentKey> getAttachmentKeys() {
        return Collections.unmodifiableSet(this.attachments.keySet());
    }

    @Override
    public <T> void addAttachment(AttachmentKey key, T value) {
        this.attachments.putIfAbsent(key, value);
    }

    @Override
    public <T> void setAttachment(AttachmentKey key, T value) {
        this.attachments.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttachment(AttachmentKey key) {
        return (T) this.attachments.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttachment(AttachmentKey key, T defaultValue) {
        return (T) this.attachments.getOrDefault(key, defaultValue);
    }

    @Override
    public void copyAttachable(Attachable attachable) {
        if (Objects.nonNull(attachable)) {
            Map<AttachmentKey, Object> map = attachable.getAttachments();
            if (Objects.nonNull(map)) {
                attachments.putAll(map);
            }
        }
    }
}
