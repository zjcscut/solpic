package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.DisposableBean;
import cn.vlts.solpic.core.spi.InitialingBean;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default thread pool.
 *
 * @author throwable
 * @since 2024/7/21 21:47
 */
public class DefaultThreadPool implements ThreadPool, InitialingBean, DisposableBean {

    private static final String WORKER_PREFIX = "solpic-default-worker-";

    private static final int SHUTDOWN_AWAIT_MILLIS = 3000;

    private static final int SHUTDOWN_AWAIT_TIMES = 3;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final AtomicInteger counter = new AtomicInteger();

    private ExecutorService executorService;

    @Override
    public String name() {
        return ThreadPool.DEFAULT;
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
            int n = Runtime.getRuntime().availableProcessors();
            executorService = new ThreadPoolExecutor(n, n, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                    task -> {
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.setPriority(Thread.NORM_PRIORITY);
                        thread.setName(WORKER_PREFIX + counter.getAndIncrement());
                        return thread;
                    });
        }
    }

    @Override
    public void destroy() {
        if (running.compareAndSet(true, false)) {
            executorService.shutdown();
            boolean exit = false;
            for (int i = 0; i < SHUTDOWN_AWAIT_TIMES; i++) {
                try {
                    if (executorService.isTerminated() ||
                            executorService.awaitTermination(SHUTDOWN_AWAIT_MILLIS, TimeUnit.MILLISECONDS)) {
                        exit = true;
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (!exit) {
                executorService.shutdownNow();
            }
        }
    }
}
