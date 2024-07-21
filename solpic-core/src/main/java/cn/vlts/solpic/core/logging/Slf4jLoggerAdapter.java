package cn.vlts.solpic.core.logging;


/**
 * Logback logger adapter.
 *
 * @author throwable
 * @since 2024/7/20 23:56
 */
public class Slf4jLoggerAdapter implements LoggerAdapter {

    public static final String NAME = "slf4j";

    private static final org.slf4j.Logger ROOT_LOGGER =
            org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    private final LogLevel logLevel;

    public Slf4jLoggerAdapter() {
        this.logLevel = fromSlf4jLevel();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    @Override
    public Logger getLogger(String key) {
        return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(key));
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        throw new UnsupportedOperationException();
    }

    private LogLevel fromSlf4jLevel() {
        if (ROOT_LOGGER.isTraceEnabled()) {
            return LogLevel.TRACE;
        }
        if (ROOT_LOGGER.isDebugEnabled()) {
            return LogLevel.DEBUG;
        }
        if (ROOT_LOGGER.isInfoEnabled()) {
            return LogLevel.INFO;
        }
        if (ROOT_LOGGER.isWarnEnabled()) {
            return LogLevel.WARN;
        }
        if (ROOT_LOGGER.isErrorEnabled()) {
            return LogLevel.ERROR;
        }
        return LogLevel.OFF;
    }
}
