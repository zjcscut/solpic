package cn.vlts.solpic.core.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base attachment key.
 * @author throwable
 * @since 2024/7/28 14:02
 */
public abstract class BaseAttachmentKey implements AttachmentKey {

    private static final AtomicInteger INDEX = new AtomicInteger();

    private final int id;

    private final String key;

    public BaseAttachmentKey() {
        this(null);
    }

    public BaseAttachmentKey(String key) {
        this.id = INDEX.incrementAndGet();
        this.key = key;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BaseAttachmentKey) {
            return this.id == ((BaseAttachmentKey) obj).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
