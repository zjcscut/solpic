package cn.vlts.solpic.core.util;

import cn.vlts.solpic.core.spi.InstanceFactory;
import cn.vlts.solpic.core.spi.SpiLoader;

import java.lang.reflect.Type;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.StreamSupport;

/**
 * Reflection utils.
 *
 * @author throwable
 * @since 2024/7/22 21:53
 */
public enum ReflectionUtils {
    X;

    private static final ConcurrentMap<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new ConcurrentHashMap<>();

    private final InstanceFactory instanceFactory = StreamSupport
            .stream(ServiceLoader.load(InstanceFactory.class).spliterator(), false)
            .min(Ordered.COMPARATOR)
            .orElseThrow(() -> new IllegalArgumentException("Load instanceFactory failed"));

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

    public boolean isClassPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, true, classLoader);
            return true;
        } catch (Throwable ignore) {

        }
        return false;
    }

    public boolean isAssignableFrom(Type type, Class<?> clazz) {
        if (type instanceof Class) {
            return clazz.isAssignableFrom((Class<?>) type);
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

    public Class<?> wrapPrimitive(Class<?> type) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(type, type);
    }

    static {
        PRIMITIVE_TO_WRAPPER.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_WRAPPER.put(byte.class, Byte.class);
        PRIMITIVE_TO_WRAPPER.put(char.class, Character.class);
        PRIMITIVE_TO_WRAPPER.put(float.class, Float.class);
        PRIMITIVE_TO_WRAPPER.put(double.class, Double.class);
        PRIMITIVE_TO_WRAPPER.put(int.class, Integer.class);
        PRIMITIVE_TO_WRAPPER.put(short.class, Short.class);
        PRIMITIVE_TO_WRAPPER.put(long.class, Long.class);
    }
}
