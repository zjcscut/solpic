package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.Spi;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Thread pool.
 *
 * @author throwable
 * @since 2024/7/19 星期五 16:10
 */
@Spi(value = "default")
public interface ThreadPool {

    String COMMON = "common";

    String DEFAULT = "default";

    String name();

    void execute(Runnable command);

    <V> Future<V> submit(Callable<V> task);
}
