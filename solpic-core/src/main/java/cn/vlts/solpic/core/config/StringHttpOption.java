package cn.vlts.solpic.core.config;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Optional;

/**
 * String HTTP option.
 *
 * @author throwable
 * @since 2024/7/22 星期一 11:43
 */
@Builder
@EqualsAndHashCode(of = "id")
public class StringHttpOption implements HttpOption<String> {

    private long id;

    private String key;

    private String propertyKey;

    private String defaultValue;

    private String description;

    private OptionLevel level;

    @Override
    public long id() {
        return id;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String propertyKey() {
        return propertyKey;
    }

    @Override
    public Class<String> valueType() {
        return String.class;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public OptionLevel level() {
        return Optional.ofNullable(level).orElse(OptionLevel.CLIENT);
    }

    @Override
    public String parseValueFromString(String content) {
        return content;
    }
}
