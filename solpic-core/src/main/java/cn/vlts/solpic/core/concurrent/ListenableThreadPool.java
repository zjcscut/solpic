package cn.vlts.solpic.core.concurrent;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Listenable thread pool.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:59
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public interface ListenableThreadPool extends Executor {

    default <V> ListenableFuture<V> execute(Runnable command, V result, FutureListener<V>... listeners) {
        return execute(command, result, this, listeners);
    }

    default <V> ListenableFuture<V> execute(Runnable command, V result, Executor executor, FutureListener<V>... listeners) {
        Callable<V> task = Executors.callable(command, result);
        return submit(task, executor, listeners);
    }

    default <V> ListenableFuture<V> submit(Callable<V> task, FutureListener<V>... listeners) {
        return submit(task, this, listeners);
    }

    default <V> ListenableFuture<V> submit(Callable<V> task, Executor executor, FutureListener<V>... listeners) {
        ListenableFutureTask<V> listenableFutureTask = ListenableFutureTask.create(task);
        if (Objects.nonNull(listeners)) {
            Stream.of(listeners).forEach(listener -> listenableFutureTask.addListener(listener, executor));
        }
        execute(listenableFutureTask);
        return listenableFutureTask;
    }
}
