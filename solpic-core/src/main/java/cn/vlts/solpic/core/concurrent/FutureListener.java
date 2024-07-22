package cn.vlts.solpic.core.concurrent;

/**
 * Listenable future listener.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:47
 */
@FunctionalInterface
public interface FutureListener<V> {

    void onDone(ListenableFuture<V> future);
}
