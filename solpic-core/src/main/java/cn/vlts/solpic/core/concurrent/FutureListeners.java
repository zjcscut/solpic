package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.logging.Logger;
import cn.vlts.solpic.core.logging.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Listenable future listeners.
 *
 * @author throwable
 * @since 2024/7/22 星期一 9:48
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class FutureListeners {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutureListeners.class);

    private ListenerExecutorPair listeners;

    private boolean fired;

    public void addListener(FutureListener listener, Executor executor) {
        synchronized (this) {
            if (!fired) {
                listeners = new ListenerExecutorPair(listener, executor, listeners);
            }
        }
    }

    public void fire(ListenableFuture future) {
        ListenerExecutorPair list;
        synchronized (this) {
            if (fired) {
                return;
            }
            fired = true;
            list = listeners;
            listeners = null;
        }
        ListenerExecutorPair reversedList = null;
        while (list != null) {
            ListenerExecutorPair tmp = list;
            list = list.next;
            tmp.next = reversedList;
            reversedList = tmp;
        }
        while (reversedList != null) {
            fireListener(future, reversedList.listener, reversedList.executor);
            reversedList = reversedList.next;
        }
    }

    private static void fireListener(ListenableFuture future, FutureListener listener, Executor executor) {
        try {
            if (Objects.nonNull(executor)) {
                executor.execute(() -> listener.onDone(future));
            } else {
                listener.onDone(future);
            }
        } catch (Throwable e) {
            LOGGER.error("Fire listener error", e);
        }
    }

    private static final class ListenerExecutorPair {

        final FutureListener listener;

        final Executor executor;

        ListenerExecutorPair next;

        ListenerExecutorPair(FutureListener listener, Executor executor, ListenerExecutorPair next) {
            this.listener = listener;
            this.executor = executor;
            this.next = next;
        }
    }
}
