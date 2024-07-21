package cn.vlts.solpic.core.logging;


import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

/**
 * Log4j logger adapter.
 *
 * @author throwable
 * @since 2024/7/20 23:56
 */
public class Log4jLoggerAdapter implements LoggerAdapter {

    public static final String NAME = "log4j";

    private LogLevel logLevel;

    public Log4jLoggerAdapter() {
        org.apache.log4j.Logger logger = LogManager.getRootLogger();
        this.logLevel = fromLog4jLevel(logger.getLevel());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new Log4jLogger(LogManager.getLogger(clazz));
    }

    @Override
    public Logger getLogger(String key) {
        return new Log4jLogger(LogManager.getLogger(key));
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        Level level = toLog4jLevel(logLevel);
        LogManager.getRootLogger().setLevel(level);
        this.logLevel = logLevel;
    }

    private LogLevel fromLog4jLevel(Level level) {
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

    private Level toLog4jLevel(LogLevel level) {
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
