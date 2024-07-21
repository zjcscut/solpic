package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.InitialingBean;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common thread pool.
 *
 * @author throwable
 * @since 2024/7/21 21:47
 */
public class CommonThreadPool implements ThreadPool, InitialingBean {

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService executorService;

    @Override
    public String name() {
        return ThreadPool.COMMON;
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        return executorService.submit(task);
    }

    @Override
    public void init() {
        if (running.compareAndSet(false, true)) {
            this.executorService = ForkJoinPool.commonPool();
        }
    }
}
