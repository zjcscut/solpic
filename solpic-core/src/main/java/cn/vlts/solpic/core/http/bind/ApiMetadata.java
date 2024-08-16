package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpMethod;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Api metadata.
 *
 * @author throwable
 * @since 2024/8/14 星期三 20:39
 */
@Data
public class ApiMetadata {

    private Class<?> type;

    private Method method;

    private String baseUrl;

    private String path;

    private String absoluteUrl;

    private HttpMethod httpMethod;

    private ContentType produce;

    private ContentType consume;

    private final Map<HttpOption<?>, Object> options = new HashMap<>();

    public <T> void addHttpOption(HttpOption<T> option, T value) {
        options.put(option, value);
    }
}
