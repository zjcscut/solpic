package cn.vlts.solpic.core.flow;

import java.util.concurrent.atomic.AtomicLong;

public final class Demand {

    private final AtomicLong val = new AtomicLong();

    public boolean increase(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("non-positive subscription request: " + n);
        }
        long prev = val.getAndAccumulate(n, (p, i) -> p + i < 0 ? Long.MAX_VALUE : p + i);
        return prev == 0;
    }

    public boolean increaseIfFulfilled() {
        return val.compareAndSet(0, 1);
    }

    public long decreaseAndGet(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        long p, d;
        do {
            d = val.get();
            p = Math.min(d, n);
        } while (!val.compareAndSet(d, d - p));
        return p;
    }

    public boolean tryDecrement() {
        return decreaseAndGet(1) == 1;
    }

    public boolean isFulfilled() {
        return val.get() == 0;
    }

    public void reset() {
        val.set(0);
    }

    public long get() {
        return val.get();
    }

    @Override
    public String toString() {
        return String.valueOf(val.get());
    }
}
