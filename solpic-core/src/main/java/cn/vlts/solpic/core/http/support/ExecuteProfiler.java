package cn.vlts.solpic.core.http.support;

import java.util.concurrent.TimeUnit;

/**
 * HTTP request execute profiler.
 *
 * @author throwable
 * @since 2024/7/22 星期一 16:47
 */
@FunctionalInterface
public interface ExecuteProfiler {

    ExecuteProfiler NO_OP = () -> 0L;

    long getCostNanos();

    default long getCostMillis() {
        long costNanos = getCostNanos();
        if (costNanos > 0L) {
            return TimeUnit.NANOSECONDS.toMillis(costNanos);
        }
        return 0L;
    }

    default void start() {

    }

    default void end() {

    }
}
