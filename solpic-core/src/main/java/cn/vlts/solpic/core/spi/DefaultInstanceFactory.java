package cn.vlts.solpic.core.spi;

import cn.vlts.solpic.core.util.Ordered;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Default instance factory.
 *
 * @author throwable
 * @since 2024/7/21 16:45
 */
public class DefaultInstanceFactory implements InstanceFactory, Ordered {

    @Override
    public <T> T newInstance(Class<T> type) throws ReflectiveOperationException {
        Constructor<T> defaultConstructor = null;
        try {
            defaultConstructor = type.getConstructor();
        } catch (NoSuchMethodException ignore) {

        }
        if (Objects.isNull(defaultConstructor)) {
            try {
                defaultConstructor = type.getDeclaredConstructor();
                if (!Modifier.isPublic(defaultConstructor.getModifiers())) {
                    defaultConstructor.setAccessible(true);
                }
            } catch (NoSuchMethodException ignore) {

            }
        }
        if (Objects.isNull(defaultConstructor)) {
            throw new ReflectiveOperationException("No default constructor found for: '" + type + "'.");
        }
        return defaultConstructor.newInstance();
    }
}
