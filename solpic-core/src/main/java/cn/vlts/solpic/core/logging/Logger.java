package cn.vlts.solpic.core.logging;

/**
 * Logger.
 *
 * @author throwable
 * @since 2024/7/20 19:08
 */
public interface Logger {

    boolean isTraceEnabled();

    void trace(String msg);

    void trace(Throwable e);

    void trace(String msg, Throwable e);

    boolean isDebugEnabled();

    void debug(String msg);

    void debug(Throwable e);

    void debug(String msg, Throwable e);

    boolean isInfoEnabled();

    void info(String msg);

    void info(Throwable e);

    void info(String msg, Throwable e);

    boolean isWarnEnabled();

    void warn(String msg);

    void warn(Throwable e);

    void warn(String msg, Throwable e);

    boolean isErrorEnabled();

    void error(String msg);

    void error(Throwable e);

    void error(String msg, Throwable e);
}
