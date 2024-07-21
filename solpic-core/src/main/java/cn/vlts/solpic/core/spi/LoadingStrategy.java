package cn.vlts.solpic.core.spi;

import cn.vlts.solpic.core.util.Ordered;

/**
 * The SPI loading strategy.
 *
 * @author throwable
 * @since 2024/7/19 星期五 15:18
 */
public interface LoadingStrategy extends Ordered {

    String name();

    String location();

    default String[] includePackages() {
        return null;
    }

    default String[] excludePackages() {
        return null;
    }

    default boolean contextClassLoaderPreferred() {
        return true;
    }

    default boolean spiLoaderClassLoaderPreferred() {
        return false;
    }

    default boolean overridden() {
        return false;
    }

    default ClassLoader classLoader() {
        return null;
    }
}
