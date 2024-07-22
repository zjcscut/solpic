package cn.vlts.solpic.core.http.support;

/**
 * Default HTTP request execute profiler.
 *
 * @author throwable
 * @since 2024/7/22 星期一 17:11
 */
public class DefaultExecuteProfiler implements ExecuteProfiler {

    private long start;

    private long cost;

    @Override
    public long getCostNanos() {
        return this.cost;
    }

    @Override
    public void end() {
        this.cost = System.nanoTime() - this.start;
    }

    @Override
    public void start() {
        this.start = System.nanoTime();
    }
}
