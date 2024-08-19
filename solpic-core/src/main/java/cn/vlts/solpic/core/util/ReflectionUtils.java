package cn.vlts.solpic.core.util;

import cn.vlts.solpic.core.spi.InstanceFactory;
import cn.vlts.solpic.core.spi.SpiLoader;
import lombok.Data;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
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

    private static final SimpleLRUCache<Type, ParameterizedTypeInfo> PTI_CACHE = new SimpleLRUCache<>(64);

    private static Method PRIVATE_LOOKUP_IN_METHOD = null;

    private static Constructor<MethodHandles.Lookup> LOOKUP_CONSTRUCTOR;

    private static final int ALLOWED_MODES;

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

    public Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalArgumentException("Unsupported type to find its raw type: " + type);
            }
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }
        throw new IllegalArgumentException("Unsupported type to find its raw type: " + type);
    }

    public Type getParameterizedIndexType(int idx, ParameterizedType pt) {
        Type[] types = pt.getActualTypeArguments();
        if (idx < 0 || idx >= types.length) {
            throw new IllegalArgumentException("Index out of bounds: " + idx);
        }
        Type targetType = types[idx];
        if (targetType instanceof WildcardType) {
            return ((WildcardType) targetType).getUpperBounds()[0];
        }
        return targetType;
    }

    public ParameterizedTypeInfo getParameterizedTypeInfo(Type type) {
        return PTI_CACHE.computeIfAbsent(type, t -> {
            ParameterizedTypeInfo info = new ParameterizedTypeInfo();
            info.setMaxDepth(0);
            info.setRootType(t);
            info.setItems(new ArrayList<>());
            AtomicInteger depth = new AtomicInteger();
            if (t instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) t;
                parseParameterizedTypeInfo(info, pt, depth, 0);
                info.setMaxDepth(info.getItems().size());
            } else if (t instanceof Class) {
                info.setMaxDepth(1);
                ParameterizedTypeItem classItem = new ParameterizedTypeItem();
                classItem.setDepth(1);
                classItem.setPosition(0);
                classItem.setRawClass((Class<?>) t);
                classItem.setRawType(t);
                info.getItems().add(classItem);
            }
            return info;
        });
    }

    private void parseParameterizedTypeInfo(ParameterizedTypeInfo info, ParameterizedType pt, AtomicInteger depth, int position) {
        int d = depth.incrementAndGet();
        ParameterizedTypeItem item = new ParameterizedTypeItem();
        info.getItems().add(item);
        Type rawType = pt.getRawType();
        Type ownerType = pt.getOwnerType();
        Type[] actualTypeArguments = pt.getActualTypeArguments();
        if (actualTypeArguments.length > 0) {
            item.setActualTypes(new ArrayList<>(Arrays.asList(actualTypeArguments)));
            for (int p = 0; p < actualTypeArguments.length; p++) {
                Type actualTypeArgument = actualTypeArguments[p];
                if (actualTypeArgument instanceof ParameterizedType) {
                    ParameterizedType ppt = (ParameterizedType) actualTypeArgument;
                    parseParameterizedTypeInfo(info, ppt, depth, p);
                } else if (actualTypeArgument instanceof Class) {
                    int classDepth = d + 1;
                    Class klass = (Class) actualTypeArgument;
                    ParameterizedTypeItem classItem = new ParameterizedTypeItem();
                    classItem.setDepth(classDepth);
                    classItem.setPosition(p);
                    classItem.setRawType(klass);
                    classItem.setRawClass(klass);
                    classItem.setActualTypes(null);
                    info.getItems().add(classItem);
                }
            }
        }
        item.setDepth(d);
        item.setPosition(position);
        item.setOwnerType(ownerType);
        item.setRawType(rawType);
        if (rawType instanceof Class) {
            item.setRawClass((Class<?>) rawType);
        }
    }

    public MethodHandles.Lookup lookup(Class<?> callerClass) {
        if (Objects.nonNull(PRIVATE_LOOKUP_IN_METHOD)) {
            try {
                return (MethodHandles.Lookup) PRIVATE_LOOKUP_IN_METHOD.invoke(MethodHandles.class,
                        callerClass, MethodHandles.lookup());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        try {
            return LOOKUP_CONSTRUCTOR.newInstance(callerClass, ALLOWED_MODES);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public MethodHandle getSpecialMethodHandle(Method method) {
        final Class<?> declaringClass = method.getDeclaringClass();
        MethodHandles.Lookup lookup = lookup(declaringClass);
        try {
            return lookup.unreflectSpecial(method, declaringClass);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Data
    public static class ParameterizedTypeInfo {

        private int maxDepth;

        private List<ParameterizedTypeItem> items;

        private Type rootType;

        public Class<?> getRawClass(int depth, int position) {
            return items.stream().filter(item -> item.getDepth() == depth && item.getPosition() == position)
                    .findFirst()
                    .map(ParameterizedTypeItem::getRawClass)
                    .orElse(null);
        }

        public Type getRawType(int depth, int position) {
            return items.stream().filter(item -> item.getDepth() == depth && item.getPosition() == position)
                    .findFirst()
                    .map(ParameterizedTypeItem::getRawType)
                    .orElse(null);
        }

        public int getMaxDepth() {
            return maxDepth;
        }
    }

    @Data
    public static class ParameterizedTypeItem {

        private int depth;

        private int position;

        private List<Type> actualTypes;

        private Type rawType;

        private Type ownerType;

        private Class<?> rawClass;
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
        if (PlatformUtils.X.getMajorVersion() >= 9) {
            try {
                // >= jdk9
                PRIVATE_LOOKUP_IN_METHOD = MethodHandles.class.getMethod("privateLookupIn", Class.class,
                        MethodHandles.Lookup.class);
            } catch (Throwable ignore) {

            }
        }
        if (Objects.isNull(PRIVATE_LOOKUP_IN_METHOD)) {
            try {
                LOOKUP_CONSTRUCTOR = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                LOOKUP_CONSTRUCTOR.setAccessible(true);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        // all modes
        ALLOWED_MODES = MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC;
    }
}
