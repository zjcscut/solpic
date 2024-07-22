package cn.vlts.solpic.core.util;

import java.util.Objects;

/**
 * Case Insensitive String.
 *
 * @author throwable
 * @since 2024/7/19 星期五 10:47
 */
public class Cis {

    private static final String EMPTY = "";

    private static final SimpleLRUCache<String, Cis> CACHE = new SimpleLRUCache<>(64);

    private final String value;

    private String folded;

    private int hash;

    public static Cis of(String string) {
        return Objects.nonNull(string) ? CACHE.computeIfAbsent(string, Cis::new) : new Cis(null);
    }

    private Cis(String value) {
        this.value = value;
        init();
    }

    private void init() {
        if (Objects.isNull(this.value)) {
            this.folded = EMPTY;
            this.hash = 0;
        } else {
            char[] chars = this.value.toCharArray();
            for (int i = chars.length - 1; i >= 0; --i) {
                chars[i] = Character.toLowerCase(Character.toUpperCase(chars[i]));
            }
            this.folded = new String(chars);
            this.hash = folded.hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (Objects.isNull(o) || getClass() != o.getClass()) {
            return false;
        }
        Cis that = (Cis) o;
        return Objects.equals(this.folded, that.folded);
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
