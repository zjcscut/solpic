package cn.vlts.solpic.core.logging;

/**
 * Logger adapter.
 *
 * @author throwable
 * @since 2024/7/20 23:49
 */
public interface LoggerAdapter {

    Logger getLogger(Class<?> clazz);

    Logger getLogger(String key);

    LogLevel getLogLevel();

    void setLogLevel(LogLevel logLevel);

    String getName();
}
