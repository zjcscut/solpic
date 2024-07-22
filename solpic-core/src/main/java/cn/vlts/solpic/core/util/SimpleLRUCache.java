package cn.vlts.solpic.core.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU cache impl.
 *
 * @author throwable
 * @since 2024/7/19 星期五 11:27
 */
public class SimpleLRUCache<K, V> extends LinkedHashMap<K, V> {

    private final int cacheSize;

    public SimpleLRUCache(int cacheSize) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cacheSize;
    }
}