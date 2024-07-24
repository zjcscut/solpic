package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.InitialingBean;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default scheduled thread pool.
 *
 * @author throwable
 * @since 2024/7/24 星期三 10:04
 */
public class DefaultScheduledThreadPool extends ThreadPoolLifecycleSupport implements ScheduledThreadPool, InitialingBean {

    private static final String WORKER_PREFIX = "solpic-scheduled-worker-";

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        ScheduledThreadPoolExecutor executor = getExecutorService();
        return executor.schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledThreadPoolExecutor executor = getExecutorService();
        return executor.schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        ScheduledThreadPoolExecutor executor = getExecutorService();
        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        ScheduledThreadPoolExecutor executor = getExecutorService();
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void init() {
        createExecutorService(() -> {
            int n = Runtime.getRuntime().availableProcessors();
            return new ScheduledThreadPoolExecutor(n, task -> {
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY);
                thread.setName(WORKER_PREFIX + counter.getAndIncrement());
                return thread;
            });
        });
    }
}
