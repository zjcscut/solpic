package cn.vlts.solpic.core.logging;


/**
 * Apache commons logging logger adapter.
 *
 * @author throwable
 * @since 2024/7/20 23:56
 */
public class JclLoggerAdapter implements LoggerAdapter {

    public static final String NAME = "jcl";

    public JclLoggerAdapter() {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new JclLogger(org.apache.commons.logging.LogFactory.getLog(clazz));
    }

    @Override
    public Logger getLogger(String key) {
        return new JclLogger(org.apache.commons.logging.LogFactory.getLog(key));
    }

    @Override
    public LogLevel getLogLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        throw new UnsupportedOperationException();
    }
}
