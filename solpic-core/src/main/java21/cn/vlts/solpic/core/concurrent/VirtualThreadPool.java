package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.InitialingBean;

import java.util.concurrent.*;

/**
 * Thread pool base on virtual thread.
 *
 * @author throwable
 * @since 2024/8/21 星期三 10:06
 */
public class VirtualThreadPool extends ThreadPoolLifecycleSupport implements ThreadPool, ListenableThreadPool, InitialingBean {

    private static final String NAME = "virtual";

    @Override
    public String name() {
        return NAME;
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
        createExecutorService(Executors::newVirtualThreadPerTaskExecutor);
    }
}
