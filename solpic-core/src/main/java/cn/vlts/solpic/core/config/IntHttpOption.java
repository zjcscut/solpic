package cn.vlts.solpic.core.config;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Optional;

/**
 * Integer HTTP option.
 *
 * @author throwable
 * @since 2024/7/22 星期一 11:43
 */
@Builder
@EqualsAndHashCode(of = "id")
public class IntHttpOption implements HttpOption<Integer> {

    private long id;

    private String key;

    private String propertyKey;

    private Integer defaultValue;

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
    public Class<Integer> valueType() {
        return Integer.class;
    }

    @Override
    public Integer defaultValue() {
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
    public Integer parseValueFromString(String content) {
        return Integer.parseInt(content);
    }
}
