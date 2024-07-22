package cn.vlts.solpic.core.config;

import lombok.Builder;

import java.util.Objects;
import java.util.Optional;

/**
 * Boolean HTTP option.
 *
 * @author throwable
 * @since 2024/7/22 星期一 11:43
 */
@Builder
public class BoolHttpOption implements HttpOption<Boolean> {

    private long id;

    private String key;

    private String propertyKey;

    private Boolean defaultValue;

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
    public Class<Boolean> valueType() {
        return Boolean.class;
    }

    @Override
    public Boolean defaultValue() {
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
    public Boolean parseValueFromString(String content) {
        if (Objects.equals("true", content) || Objects.equals("1", content)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
