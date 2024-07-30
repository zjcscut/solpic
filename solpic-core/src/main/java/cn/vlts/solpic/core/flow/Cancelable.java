package cn.vlts.solpic.core.flow;

@FunctionalInterface
public interface Cancelable {

    boolean cancel(boolean mayInterruptIfRunning);
}
