package cn.vlts.solpic.core.flow;

import java.util.Iterator;
import java.util.Objects;

public final class PullPublisher<T> implements Publisher<T> {

    private final Iterable<T> iterable;
    private final Throwable throwable;

    public PullPublisher(Iterable<T> iterable, Throwable throwable) {
        this.iterable = iterable;
        this.throwable = throwable;
    }

    public PullPublisher(Iterable<T> iterable) {
        this(iterable, null);
    }

    public PullPublisher(Throwable throwable) {
        this(null, throwable);
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        PullSubscription sub;
        if (Objects.nonNull(throwable)) {
            sub = new PullSubscription(subscriber, null, throwable);
        } else {
            sub = new PullSubscription(subscriber, iterable.iterator(), null);
        }
        subscriber.onSubscribe(sub);
        if (Objects.nonNull(throwable)) {
            sub.pullScheduler.runOrSchedule();
        }
    }

    private class PullSubscription implements Subscription {

        private final Subscriber<? super T> subscriber;
        private final Iterator<T> iter;
        private volatile boolean completed;
        private volatile boolean cancelled;
        private volatile Throwable error;
        final SequentialScheduler pullScheduler = new SequentialScheduler(new PullTask());
        private final Demand demand = new Demand();

        PullSubscription(Subscriber<? super T> subscriber,
                         Iterator<T> iter,
                         Throwable throwable) {
            this.subscriber = subscriber;
            this.iter = iter;
            this.error = throwable;
        }

        final class PullTask extends SequentialScheduler.CompleteRestartableTask {

            @Override
            protected void run() {
                if (completed || cancelled) {
                    return;
                }
                Throwable t = error;
                if (Objects.nonNull(t)) {
                    completed = true;
                    pullScheduler.stop();
                    subscriber.onError(t);
                    return;
                }

                while (demand.tryDecrement() && !cancelled) {
                    T next;
                    try {
                        if (!iter.hasNext()) {
                            break;
                        }
                        next = iter.next();
                    } catch (Throwable t1) {
                        completed = true;
                        pullScheduler.stop();
                        subscriber.onError(t1);
                        return;
                    }
                    subscriber.onNext(next);
                }
                if (!iter.hasNext() && !cancelled) {
                    completed = true;
                    pullScheduler.stop();
                    subscriber.onComplete();
                }
            }
        }

        @Override
        public void request(long n) {
            if (cancelled) {
                return;
            }
            if (n <= 0) {
                error = new IllegalArgumentException("non-positive subscription request: " + n);
            } else {
                demand.increase(n);
            }
            pullScheduler.runOrSchedule();
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}

