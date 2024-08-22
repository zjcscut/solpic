package cn.vlts.solpic.core.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * Stats factor.
 *
 * @author throwable
 * @since 2024/8/8 星期四 10:42
 */
class StatsFactor {

    private final LongAdder counter = new LongAdder();

    public long getValue() {
        return counter.sum();
    }

    public void increment() {
        counter.increment();
    }

    public void decrement() {
        counter.decrement();
    }

    public void add(long v) {
        counter.add(v);
    }

    public void set(long v) {
        reset();
        counter.add(v);
    }

    public void reset() {
        counter.reset();
    }
}
