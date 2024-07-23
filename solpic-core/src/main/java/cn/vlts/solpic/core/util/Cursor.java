package cn.vlts.solpic.core.util;

/**
 * Cursor.
 *
 * @author throwable
 * @since 2024/7/23 星期二 10:56
 */
public final class Cursor {

    private int pos;

    private final int lower;

    private final int upper;

    public Cursor(int upper) {
        this(0, upper);
    }

    public Cursor(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
        this.pos = lower;
        if (lower < 0) {
            throw new IllegalArgumentException("lower");
        }
        if (upper <= lower) {
            throw new IllegalArgumentException("upper");
        }
    }

    public int lower() {
        return lower;
    }

    public int upper() {
        return upper;
    }

    public int pos() {
        return pos;
    }

    public void updatePos(int pos) {
        if (pos < lower || pos > upper) {
            throw new IllegalArgumentException("pos");
        }
        this.pos = pos;
    }

    public boolean isEnd() {
        return this.pos >= this.upper;
    }

    @Override
    public String toString() {
        return "[" + this.lower + ">" + this.pos + ">" + this.upper + "]";
    }
}
