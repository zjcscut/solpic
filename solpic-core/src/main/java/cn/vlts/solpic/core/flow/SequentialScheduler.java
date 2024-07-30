package cn.vlts.solpic.core.flow;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

public final class SequentialScheduler {

    public abstract static class DeferredCompleter {

        private DeferredCompleter() {
        }

        public abstract void complete();
    }

    @FunctionalInterface
    public interface RestartableTask {

        void run(DeferredCompleter taskCompleter);
    }

    public abstract static class CompleteRestartableTask
            implements RestartableTask {
        @Override
        public final void run(DeferredCompleter taskCompleter) {
            try {
                run();
            } finally {
                taskCompleter.complete();
            }
        }

        protected abstract void run();
    }

    public static final class LockingRestartableTask
            extends CompleteRestartableTask {

        private final Runnable mainLoop;
        private final Lock lock = new ReentrantLock();

        public LockingRestartableTask(Runnable mainLoop) {
            this.mainLoop = mainLoop;
        }

        @Override
        protected void run() {
            boolean locked = lock.tryLock();
            try {
                mainLoop.run();
            } finally {
                if (locked) lock.unlock();
            }
        }
    }

    private static final int OFFLOAD = 1;
    private static final int AGAIN = 2;
    private static final int BEGIN = 4;
    private static final int STOP = 8;
    private static final int END = 16;

    private final AtomicInteger state = new AtomicInteger(END);
    private final RestartableTask restartableTask;
    private final DeferredCompleter completer;
    private final SchedulableTask schedulableTask;

    private final class SchedulableTask implements Runnable {
        @Override
        public void run() {
            restartableTask.run(completer);
        }
    }

    public SequentialScheduler(RestartableTask restartableTask) {
        this.restartableTask = requireNonNull(restartableTask);
        this.completer = new TryEndDeferredCompleter();
        this.schedulableTask = new SchedulableTask();
    }

    public void runOrSchedule() {
        runOrSchedule(schedulableTask, null);
    }

    public void runOrSchedule(Executor executor) {
        runOrSchedule(schedulableTask, executor);
    }

    private void runOrSchedule(SchedulableTask task, Executor executor) {
        while (true) {
            int s = state.get();
            if (s == END) {
                if (state.compareAndSet(END, BEGIN)) {
                    break;
                }
            } else if ((s & BEGIN) != 0) {
                if (state.compareAndSet(s, AGAIN | (s & OFFLOAD))) {
                    return;
                }
            } else if ((s & AGAIN) != 0 || s == STOP) {
                return;
            } else {
                throw new InternalError(String.valueOf(s));
            }
        }
        if (executor == null) {
            task.run();
        } else {
            executor.execute(task);
        }
    }

    private class TryEndDeferredCompleter extends DeferredCompleter {

        @Override
        public void complete() {
            while (true) {
                int s;
                while (((s = state.get()) & OFFLOAD) != 0) {
                    if (state.compareAndSet(s, s & ~OFFLOAD)) {
                        return;
                    }
                }
                while (true) {
                    if ((s & OFFLOAD) != 0) {
                        throw new InternalError(String.valueOf(s));
                    }
                    if (s == BEGIN) {
                        if (state.compareAndSet(BEGIN, END)) {
                            return;
                        }
                    } else if (s == AGAIN) {
                        if (state.compareAndSet(AGAIN, BEGIN | OFFLOAD)) {
                            break;
                        }
                    } else if (s == STOP) {
                        return;
                    } else if (s == END) {
                        throw new IllegalStateException("Duplicate completion");
                    } else {
                        throw new InternalError(String.valueOf(s));
                    }
                    s = state.get();
                }
                restartableTask.run(completer);
            }
        }
    }

    public boolean isStopped() {
        return state.get() == STOP;
    }

    public void stop() {
        state.set(STOP);
    }

    public static SequentialScheduler lockingScheduler(Runnable mainLoop) {
        return new SequentialScheduler(new LockingRestartableTask(mainLoop));
    }
}
