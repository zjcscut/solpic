package cn.vlts.solpic.core.config;

import lombok.Builder;
import lombok.EqualsAndHashCode;

/**
 * Proxy option.
 *
 * @author throwable
 * @since 2024/7/22 星期一 15:46
 */
@Builder
@EqualsAndHashCode(of = "id")
public class ProxyHttpOption implements HttpOption<ProxyConfig> {

    private long id;

    private String key;

    private String propertyKey;

    private ProxyConfig defaultValue;

    private String description;

    private OptionLevel level;

    @Override
    public String description() {
        return description;
    }

    @Override
    public ProxyConfig defaultValue() {
        return defaultValue;
    }

    @Override
    public Class<ProxyConfig> valueType() {
        return ProxyConfig.class;
    }

    @Override
    public String propertyKey() {
        return propertyKey;
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
    public ProxyConfig parseValueFromString(String content) {
        return ProxyConfig.create(content);
    }
}
