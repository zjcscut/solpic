package cn.vlts.solpic.core.config;

/**
 * Shutdown hook action. Callback when ShutdownHook is invoked.
 *
 * @author throwable
 * @since 2024/7/21 23:30
 */
@FunctionalInterface
public interface ShutdownHookAction {

    /**
     * Callback on shutdown.
     */
    void doOnShutdown() throws Exception;
}
