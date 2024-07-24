package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.Spi;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled thread pool.
 *
 * @author throwable
 * @since 2024/7/24 星期三 9:55
 */
@Spi(value = ScheduledThreadPool.DEFAULT)
public interface ScheduledThreadPool {

    String DEFAULT = "default";

    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
