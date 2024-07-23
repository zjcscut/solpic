package cn.vlts.solpic.core.flow;

/**
 * Cancelable.
 *
 * @author throwable
 * @since 2024/7/24 00:25
 */
@FunctionalInterface
public interface Cancelable {

    boolean cancel(boolean mayInterruptIfRunning);
}
