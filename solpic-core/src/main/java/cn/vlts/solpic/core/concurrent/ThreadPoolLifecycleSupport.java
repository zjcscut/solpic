package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.DisposableBean;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Thread pool lifecycle support.
 *
 * @author throwable
 * @version v1
 * @since 2024/7/24 星期三 10:06
 */
public abstract class ThreadPoolLifecycleSupport implements DisposableBean {

    protected static final int DEFAULT_SHUTDOWN_AWAIT_MILLIS = 3000;

    protected static final int DEFAULT_SHUTDOWN_AWAIT_TIMES = 3;

    protected final AtomicBoolean running = new AtomicBoolean(false);

    protected final AtomicReference<ExecutorService> executorServiceRef = new AtomicReference<>();

    protected <T extends ExecutorService> T createExecutorService(Supplier<T> executorServiceSupplier) {
        T executorService;
        if (running.compareAndSet(false, true) &&
                executorServiceRef.compareAndSet(null, (executorService = executorServiceSupplier.get()))) {
            return executorService;
        }
        throw new IllegalStateException("Create executor service failed");
    }

    protected <T extends ExecutorService> T getExecutorService() {
        return (T) Optional.ofNullable(executorServiceRef.get())
                .orElseThrow(() -> new IllegalStateException("Get executor service failed"));
    }

    protected boolean isRunning() {
        return running.get();
    }

    protected Integer getShutdownAwaitMills() {
        return null;
    }

    protected Integer getShutdownAwaitTimes() {
        return null;
    }

    @Override
    public void destroy() {
        if (running.compareAndSet(true, false)) {
            ExecutorService executorService = executorServiceRef.getAndSet(null);
            if (Objects.nonNull(executorService)) {
                executorService.shutdown();
                boolean exit = false;
                int shutdownAwaitMills = Optional.ofNullable(getShutdownAwaitMills()).orElse(DEFAULT_SHUTDOWN_AWAIT_MILLIS);
                int shutdownAwaitTimes = Optional.ofNullable(getShutdownAwaitTimes()).orElse(DEFAULT_SHUTDOWN_AWAIT_TIMES);
                for (int i = 0; i < shutdownAwaitTimes; i++) {
                    try {
                        if (executorService.isTerminated() ||
                                executorService.awaitTermination(shutdownAwaitMills, TimeUnit.MILLISECONDS)) {
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
}
