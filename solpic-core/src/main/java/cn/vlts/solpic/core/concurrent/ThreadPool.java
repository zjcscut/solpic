package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.Spi;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Thread pool.
 *
 * @author throwable
 * @since 2024/7/19 星期五 16:10
 */
@Spi(value = ThreadPool.DEFAULT)
public interface ThreadPool extends Executor, ListenableThreadPool {

    String COMMON = "common";

    String DEFAULT = "default";

    String name();

    @Override
    void execute(Runnable command);

    <V> Future<V> submit(Callable<V> task);
}
