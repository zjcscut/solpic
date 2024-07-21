package cn.vlts.solpic.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Key Case Insensitive HashMap.
 *
 * @author throwable
 * @since 2024/7/19 星期五 10:44
 */
@SuppressWarnings("unchecked")
public class CaseInsensitiveMap<K extends Cis, V> extends HashMap<K, V> {

    public CaseInsensitiveMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CaseInsensitiveMap(int initialCapacity) {
        super(initialCapacity);
    }

    public CaseInsensitiveMap() {
        super(16, 0.75F);
    }

    public CaseInsensitiveMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public V put0(String key, V value) {
        return put((K) Cis.of(key), value);
    }

    public V putIfAbsent0(String key, V value) {
        return putIfAbsent((K) Cis.of(key), value);
    }

    public V get0(String key) {
        return get(Cis.of(key));
    }

    public V getOrDefault0(String key, V defaultValue) {
        return getOrDefault(Cis.of(key), defaultValue);
    }
}
