package cn.vlts.solpic.core.util;

import cn.vlts.solpic.core.spi.DefaultInstanceFactory;
import cn.vlts.solpic.core.spi.InstanceFactory;

/**
 * Reflection utils.
 *
 * @author throwable
 * @since 2024/7/22 21:53
 */
public enum ReflectionUtils {
    X;

    private final InstanceFactory instanceFactory = new DefaultInstanceFactory();

    public Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable ignore) {

        }
        return false;
    }

    public <T> T createInstance(Class<T> clazz) {
        try {
            return instanceFactory.newInstance(clazz);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}