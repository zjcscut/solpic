package cn.vlts.solpic.core.config;

import cn.vlts.solpic.core.http.support.ExecuteProfiler;
import lombok.Builder;

/**
 * HTTP execute profiler option.
 *
 * @author throwable
 * @since 2024/7/22 星期一 15:46
 */
@Builder
public class ExecuteProfilerHttpOption implements HttpOption<ExecuteProfiler> {

    private long id;

    private String key;

    private String propertyKey;

    private ExecuteProfiler defaultValue;

    private String description;

    private OptionLevel level;

    @Override
    public String description() {
        return description;
    }

    @Override
    public ExecuteProfiler defaultValue() {
        return defaultValue;
    }

    @Override
    public Class<ExecuteProfiler> valueType() {
        return ExecuteProfiler.class;
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
    public ExecuteProfiler parseValueFromString(String content) {
        return null;
    }
}
