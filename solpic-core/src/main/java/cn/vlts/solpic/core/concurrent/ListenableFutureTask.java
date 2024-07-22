package cn.vlts.solpic.core.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * Listenable future task.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:48
 */
public class ListenableFutureTask<V> extends FutureTask<V> implements ListenableFuture<V> {

    private final FutureListeners listeners = new FutureListeners();

    private ListenableFutureTask(Callable<V> callable) {
        super(callable);
    }

    private ListenableFutureTask(Runnable runnable, V result) {
        super(runnable, result);
    }

    public static <V> ListenableFutureTask<V> create(Callable<V> callable) {
        return new ListenableFutureTask<>(callable);
    }

    public static <V> ListenableFutureTask<V> create(Runnable runnable, V result) {
        return new ListenableFutureTask<>(runnable, result);
    }

    @Override
    public void addListener(FutureListener<V> listener, Executor executor) {
        listeners.addListener(listener, executor);
    }

    @Override
    public void addListener(Runnable runnable, Executor executor) {
        RunnableFutureListener<?> listener = RunnableFutureListener.create(runnable);
        listeners.addListener(listener, executor);
    }

    @Override
    protected void done() {
        listeners.fire(this);
    }
}
