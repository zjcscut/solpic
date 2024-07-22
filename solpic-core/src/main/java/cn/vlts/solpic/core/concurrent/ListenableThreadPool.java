package cn.vlts.solpic.core.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * Listenable thread pool.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:59
 */
public interface ListenableThreadPool extends Executor {

    default <V> ListenableFuture<V> execute(Runnable command, V result, FutureListener<V>... listeners) {
        return execute(command, result, this, listeners);
    }

    <V> ListenableFuture<V> execute(Runnable command, V result, Executor executor, FutureListener<V>... listeners);

    default <V> ListenableFuture<V> submit(Callable<V> task, FutureListener<V>... listeners) {
        return submit(task, this, listeners);
    }

    <V> ListenableFuture<V> submit(Callable<V> task, Executor executor, FutureListener<V>... listeners);
}
