package cn.vlts.solpic.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import cn.vlts.solpic.core.util.ArgumentUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Logback logger adapter.
 *
 * @author throwable
 * @since 2024/7/20 23:56
 */
public class LogbackLoggerAdapter implements LoggerAdapter {

    private static final String BINDER_CLASS = "org.slf4j.impl.StaticLoggerBinder";

    private static ILoggerFactory BINDER_LOGGER_FACTORY = null;

    private static final String ROOT_LOGGER_NAME = "ROOT";

    public static final String NAME = "logback";

    private final ch.qos.logback.classic.LoggerContext loggerContext;

    private LogLevel logLevel;

    public LogbackLoggerAdapter() {
        this.loggerContext = (LoggerContext) getLoggerFactory();
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

    private ILoggerFactory getLoggerFactory() {
        ILoggerFactory loggerFactory;
        if (Objects.nonNull(BINDER_LOGGER_FACTORY)) {
            loggerFactory = BINDER_LOGGER_FACTORY;
        } else {
            loggerFactory = LoggerFactory.getILoggerFactory();
        }
        ArgumentUtils.X.isTrue(loggerFactory instanceof LoggerContext, "LoggerFactory is not a Logback " +
                "LoggerContext but Logback is on classpath");
        return loggerFactory;
    }

    static {
        try {
            Class<?> type = Class.forName(BINDER_CLASS);
            Object binder = type.getDeclaredMethod("getSingleton").invoke(null);
            BINDER_LOGGER_FACTORY = (ILoggerFactory) type.getDeclaredMethod("getLoggerFactory").invoke(binder);
        } catch (Throwable ignore) {

        }
    }
}
