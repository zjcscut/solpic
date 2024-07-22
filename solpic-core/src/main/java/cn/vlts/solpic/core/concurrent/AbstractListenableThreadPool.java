package cn.vlts.solpic.core.concurrent;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Base listenable thread pool.
 *
 * @author throwable
 * @since 2024/7/22 星期一 10:11
 */
public abstract class AbstractListenableThreadPool implements ListenableThreadPool, Executor {

    @Override
    public <V> ListenableFuture<V> execute(Runnable command, V result, Executor executor, FutureListener<V>... listeners) {
        Callable<V> task = Executors.callable(command, result);
        return submit(task, executor, listeners);
    }

    @Override
    public <V> ListenableFuture<V> submit(Callable<V> task, Executor executor, FutureListener<V>... listeners) {
        ListenableFutureTask<V> listenableFutureTask = ListenableFutureTask.create(task);
        if (Objects.nonNull(listeners)) {
            Stream.of(listeners).forEach(listener -> listenableFutureTask.addListener(listener, executor));
        }
        execute(listenableFutureTask);
        return listenableFutureTask;
    }
}
