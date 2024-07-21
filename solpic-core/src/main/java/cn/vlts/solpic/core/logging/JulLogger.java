package cn.vlts.solpic.core.logging;

import java.util.Objects;
import java.util.logging.Level;

/**
 * Java util logging logger.
 *
 * @author throwable
 * @since 2024/7/20 23:56
 */
public class JulLogger implements Logger {

    private final java.util.logging.Logger logger;

    public JulLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINER);
    }

    @Override
    public void trace(String msg) {
        logger.log(Level.FINER, msg);
    }

    @Override
    public void trace(Throwable e) {
        logger.log(Level.FINER, Objects.isNull(e) ? null : e.getMessage(), e);
    }

    @Override
    public void trace(String msg, Throwable e) {
        logger.log(Level.FINER, msg, e);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public void debug(String msg) {
        logger.log(Level.FINE, msg);
    }

    @Override
    public void debug(Throwable e) {
        logger.log(Level.FINE, Objects.isNull(e) ? null : e.getMessage(), e);
    }

    @Override
    public void debug(String msg, Throwable e) {
        logger.log(Level.FINE, msg, e);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public void info(String msg) {
        logger.log(Level.INFO, msg);
    }

    @Override
    public void info(Throwable e) {
        logger.log(Level.INFO, Objects.isNull(e) ? null : e.getMessage(), e);
    }

    @Override
    public void info(String msg, Throwable e) {
        logger.log(Level.INFO, msg, e);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(String msg) {
        logger.log(Level.WARNING, msg);
    }

    @Override
    public void warn(Throwable e) {
        logger.log(Level.WARNING, Objects.isNull(e) ? null : e.getMessage(), e);
    }

    @Override
    public void warn(String msg, Throwable e) {
        logger.log(Level.WARNING, msg, e);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    @Override
    public void error(Throwable e) {
        logger.log(Level.SEVERE, Objects.isNull(e) ? null : e.getMessage(), e);
    }

    @Override
    public void error(String msg, Throwable e) {
        logger.log(Level.SEVERE, msg, e);
    }
}
