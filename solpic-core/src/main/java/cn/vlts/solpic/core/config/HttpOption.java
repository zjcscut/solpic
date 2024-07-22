package cn.vlts.solpic.core.config;

/**
 * HTTP option.
 *
 * @author throwable
 * @since 2024/7/22 星期一 11:37
 */
public interface HttpOption<T> {

    long id();

    String key();

    /**
     * Property key.
     */
    String propertyKey();

    Class<T> valueType();

    default T defaultValue() {
        return null;
    }

    default String description() {
        return null;
    }

    default OptionLevel level() {
        return OptionLevel.CLIENT;
    }

    default HttpOption<T> parent() {
        return null;
    }

    T parseValueFromString(String content);

    default boolean support(long opts) {
        return (opts & id()) != 0;
    }
}
