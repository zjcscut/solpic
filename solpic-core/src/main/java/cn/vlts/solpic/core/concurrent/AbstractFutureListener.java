package cn.vlts.solpic.core.concurrent;

/**
 * Base listenable future listener.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:48
 */
public abstract class AbstractFutureListener<V> implements FutureListener<V> {

    @Override
    public void onDone(ListenableFuture<V> future) {
        if (future.isDone()) {
            try {
                V result = future.get();
                onSuccess(result);
            } catch (Throwable throwable) {
                onError(throwable);
            }
        } else if (future.isCancelled()) {
            onCancel();
        }
    }

    protected void onSuccess(V result) {

    }

    protected void onError(Throwable throwable) {

    }

    protected void onCancel() {

    }
}
