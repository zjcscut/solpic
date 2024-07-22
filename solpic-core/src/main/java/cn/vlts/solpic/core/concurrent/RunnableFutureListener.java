package cn.vlts.solpic.core.concurrent;

/**
 * Listenable future listener runnable adapter.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:48
 */
public class RunnableFutureListener<V> implements FutureListener<V> {

    private final Runnable runnable;

    private RunnableFutureListener(Runnable runnable) {
        this.runnable = runnable;
    }

    public static <V> RunnableFutureListener<V> create(Runnable runnable) {
        return new RunnableFutureListener<>(runnable);
    }

    @Override
    public void onDone(ListenableFuture<V> future) {
        runnable.run();
    }
}
