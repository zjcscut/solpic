package cn.vlts.solpic.core.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Listenable future.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:48
 */
public interface ListenableFuture<V> extends Future<V> {

    void addListener(FutureListener<V> listener, Executor executor);

    default void addListener(FutureListener<V> listener) {
        addListener(listener, null);
    }

    void addListener(Runnable runnable, Executor executor);

    default void addListener(Runnable runnable) {
        addListener(runnable, null);
    }
}
