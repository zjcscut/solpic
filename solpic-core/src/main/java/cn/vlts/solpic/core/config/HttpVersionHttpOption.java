package cn.vlts.solpic.core.config;

import cn.vlts.solpic.core.http.HttpVersion;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Optional;

/**
 * HTTP version option.
 *
 * @author throwable
 * @since 2024/8/7 星期三 10:57
 */
@Builder
@EqualsAndHashCode(of = "id")
public class HttpVersionHttpOption implements HttpOption<HttpVersion> {

    private long id;

    private String key;

    private String propertyKey;

    private HttpVersion defaultValue;

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
    public Class<HttpVersion> valueType() {
        return HttpVersion.class;
    }

    @Override
    public HttpVersion defaultValue() {
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
    public HttpVersion parseValueFromString(String content) {
        return HttpVersion.fromVersion(content);
    }
}
