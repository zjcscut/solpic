package cn.vlts.solpic.core.spi;

/**
 * Instance factory.
 *
 * @author throwable
 * @since 2024/7/21 16:44
 */
@Spi
public interface InstanceFactory {

    <T> T newInstance(Class<T> type) throws ReflectiveOperationException;
}
