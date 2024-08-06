package cn.vlts.solpic.core.config;

import lombok.Builder;
import lombok.EqualsAndHashCode;

/**
 * SSL HTTP option.
 *
 * @author throwable
 * @since 2024/8/6 星期二 17:39
 */
@Builder
@EqualsAndHashCode(of = "id")
public class SSLHttpOption implements HttpOption<SSLConfig> {

    private long id;

    private String key;

    private SSLConfig defaultValue;

    private String description;

    private OptionLevel level;

    @Override
    public String description() {
        return description;
    }

    @Override
    public SSLConfig defaultValue() {
        return defaultValue;
    }

    @Override
    public Class<SSLConfig> valueType() {
        return SSLConfig.class;
    }

    @Override
    public String propertyKey() {
        return null;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public SSLConfig parseValueFromString(String content) {
        return null;
    }
}
