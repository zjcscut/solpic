package cn.vlts.solpic.core.util;

import java.util.Comparator;

/**
 * Ordered.
 *
 * @author throwable
 * @since 2024/7/19 星期五 15:02
 */
public interface Ordered extends Comparable<Ordered> {

    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    int NORMAL_PRECEDENCE = 0;

    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    Comparator<Object> COMPARATOR = (left, right) -> {
        boolean x = left instanceof Ordered;
        boolean y = right instanceof Ordered;
        if (x && !y) {
            return -1;
        }
        if (!x && y) {
            return 1;
        }
        if (x) {
            return ((Ordered) left).compareTo((Ordered) right);
        }
        return 0;
    };

    default int getOrder() {
        return NORMAL_PRECEDENCE;
    }

    @Override
    default int compareTo(Ordered other) {
        return Integer.compare(this.getOrder(), other.getOrder());
    }
}
