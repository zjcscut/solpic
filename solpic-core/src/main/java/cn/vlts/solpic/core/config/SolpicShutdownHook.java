package cn.vlts.solpic.core.config;

import cn.vlts.solpic.core.logging.Logger;
import cn.vlts.solpic.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Solpic shutdown hook.
 *
 * @author throwable
 * @since 2024/7/21 23:25
 */
public class SolpicShutdownHook extends Thread {

    private final AtomicBoolean added = new AtomicBoolean();

    private final AtomicBoolean destroyed = new AtomicBoolean();

    private static final Logger LOGGER = LoggerFactory.getLogger(SolpicShutdownHook.class);

    private static final List<ShutdownHookAction> GLOBAL_ACTIONS = new ArrayList<>();

    public SolpicShutdownHook() {
        super("SolpicShutdownHook");
    }

    @Override
    public void run() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Shutdown hook callback now");
        }
        if (destroyed.compareAndSet(false, true)) {
            GLOBAL_ACTIONS.forEach(shutdownHookAction -> {
                try {
                    shutdownHookAction.doOnShutdown();
                } catch (Throwable e) {
                    LOGGER.error("Shutdown hook action callback error", e);
                }
            });
        }
    }

    public void addToShutdownHook() {
        if (added.compareAndSet(false, true)) {
            try {
                Runtime.getRuntime().addShutdownHook(this);
            } catch (Throwable e) {
                LOGGER.warn("Failed to add shutdown hook", e);
            }
        }
    }

    public void removeFromShutdownHook() {
        if (added.compareAndSet(true, false)) {
            try {
                Runtime.getRuntime().removeShutdownHook(this);
            } catch (Throwable e) {
                LOGGER.warn("Failed to remove shutdown hook", e);
            }
        }
    }

    public boolean isAdded() {
        return added.get();
    }

    public boolean isDestroyed() {
        return destroyed.get();
    }

    public static synchronized void registerShutdownHookAction(ShutdownHookAction sha) {
        GLOBAL_ACTIONS.add(sha);
    }

    public static synchronized void unregisterShutdownHookAction(ShutdownHookAction sha) {
        GLOBAL_ACTIONS.removeIf(shutdownHookAction -> shutdownHookAction == sha);
    }
}
