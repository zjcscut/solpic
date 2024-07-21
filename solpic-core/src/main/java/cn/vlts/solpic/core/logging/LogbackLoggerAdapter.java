package cn.vlts.solpic.core.logging;

import ch.qos.logback.classic.Level;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Logback logger adapter.
 *
 * @author throwable
 * @since 2024/7/20 23:56
 */
public class LogbackLoggerAdapter implements LoggerAdapter {

    private static final String ROOT_LOGGER_NAME = "ROOT";

    public static final String NAME = "logback";

    private final ch.qos.logback.classic.LoggerContext loggerContext;

    private LogLevel logLevel;

    public LogbackLoggerAdapter() {
        StaticLoggerBinder loggerBinder = StaticLoggerBinder.getSingleton();
        this.loggerContext = (ch.qos.logback.classic.LoggerContext) loggerBinder.getLoggerFactory();
        this.logLevel = fromLogbackLevel(loggerContext.getLogger(ROOT_LOGGER_NAME).getLevel());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new LogbackLogger(loggerContext.getLogger(clazz.getName()));
    }

    @Override
    public Logger getLogger(String key) {
        return new LogbackLogger(loggerContext.getLogger(key));
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        Level level = toLogbackLevel(logLevel);
        loggerContext.getLogger(ROOT_LOGGER_NAME).setLevel(level);
        this.logLevel = logLevel;
    }

    private LogLevel fromLogbackLevel(Level level) {
        if (Level.ALL == level) {
            return LogLevel.ALL;
        }
        if (Level.TRACE == level) {
            return LogLevel.TRACE;
        }
        if (Level.DEBUG == level) {
            return LogLevel.DEBUG;
        }
        if (Level.INFO == level) {
            return LogLevel.INFO;
        }
        if (Level.WARN == level) {
            return LogLevel.WARN;
        }
        if (Level.ERROR == level) {
            return LogLevel.ERROR;
        }
        return LogLevel.OFF;
    }

    private Level toLogbackLevel(LogLevel level) {
        if (LogLevel.ALL == level) {
            return Level.ALL;
        }
        if (LogLevel.TRACE == level) {
            return Level.TRACE;
        }
        if (LogLevel.DEBUG == level) {
            return Level.DEBUG;
        }
        if (LogLevel.INFO == level) {
            return Level.INFO;
        }
        if (LogLevel.WARN == level) {
            return Level.WARN;
        }
        if (LogLevel.ERROR == level) {
            return Level.ERROR;
        }
        return Level.OFF;
    }
}
