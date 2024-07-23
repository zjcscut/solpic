package cn.vlts.solpic.core.util;

/**
 * Triple predicate.
 *
 * @author throwable
 * @since 2024/7/23 星期二 14:18
 */
@FunctionalInterface
public interface TriPredicate<T, R, U> {

    boolean test(T t, R r, U u);
}
