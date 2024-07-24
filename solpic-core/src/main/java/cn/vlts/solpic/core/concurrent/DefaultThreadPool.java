package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.InitialingBean;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default thread pool.
 *
 * @author throwable
 * @since 2024/7/21 21:47
 */
public class DefaultThreadPool extends ThreadPoolLifecycleSupport implements ThreadPool, ListenableThreadPool, InitialingBean {

    private static final String WORKER_PREFIX = "solpic-default-worker-";

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public String name() {
        return ThreadPool.DEFAULT;
    }

    @Override
    public void execute(Runnable command) {
        getExecutorService().execute(command);
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        return getExecutorService().submit(task);
    }

    @Override
    public void init() {
        createExecutorService(() -> {
            int n = Runtime.getRuntime().availableProcessors();
            return new ThreadPoolExecutor(n, n, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                    task -> {
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.setPriority(Thread.NORM_PRIORITY);
                        thread.setName(WORKER_PREFIX + counter.getAndIncrement());
                        return thread;
                    });
        });
    }
}
