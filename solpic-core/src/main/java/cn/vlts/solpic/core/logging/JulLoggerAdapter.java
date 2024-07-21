package cn.vlts.solpic.core.logging;


import java.util.logging.Level;

/**
 * Java util logging logger adapter.
 *
 * @author throwable
 * @since 2024/7/20 23:56
 */
public class JulLoggerAdapter implements LoggerAdapter {

    public static final String NAME = "jul";

    private static final String GLOBAL_LOGGER_NAME = "global";

    private LogLevel logLevel;

    public JulLoggerAdapter() {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME);
        this.logLevel = fromJulLevel(logger.getLevel());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new JulLogger(java.util.logging.Logger.getLogger(clazz.getName()));
    }

    @Override
    public Logger getLogger(String key) {
        return new JulLogger(java.util.logging.Logger.getLogger(key));
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        Level level = toJulLevel(logLevel);
        java.util.logging.Logger.getLogger(GLOBAL_LOGGER_NAME).setLevel(level);
        this.logLevel = logLevel;
    }

    private LogLevel fromJulLevel(Level level) {
        if (Level.ALL == level) {
            return LogLevel.ALL;
        }
        if (Level.FINER == level) {
            return LogLevel.TRACE;
        }
        if (Level.FINE == level) {
            return LogLevel.DEBUG;
        }
        if (Level.INFO == level) {
            return LogLevel.INFO;
        }
        if (Level.WARNING == level) {
            return LogLevel.WARN;
        }
        if (Level.SEVERE == level) {
            return LogLevel.ERROR;
        }
        return LogLevel.OFF;
    }

    private Level toJulLevel(LogLevel level) {
        if (LogLevel.ALL == level) {
            return Level.ALL;
        }
        if (LogLevel.TRACE == level) {
            return Level.FINER;
        }
        if (LogLevel.DEBUG == level) {
            return Level.FINE;
        }
        if (LogLevel.INFO == level) {
            return Level.INFO;
        }
        if (LogLevel.WARN == level) {
            return Level.WARNING;
        }
        if (LogLevel.ERROR == level) {
            return Level.SEVERE;
        }
        return Level.OFF;
    }
}
