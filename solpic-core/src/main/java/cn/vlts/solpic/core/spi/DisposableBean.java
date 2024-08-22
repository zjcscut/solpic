package cn.vlts.solpic.core.spi;

/**
 * Disposable bean.
 *
 * @author throwable
 * @since 2024/7/20 00:22
 */
@FunctionalInterface
public interface DisposableBean {

    void destroy() throws Exception;
}
