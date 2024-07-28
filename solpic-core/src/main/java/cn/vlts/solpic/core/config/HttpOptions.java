package cn.vlts.solpic.core.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Internal HTTP options.
 *
 * @author throwable
 * @since 2024/7/22 星期一 11:48
 */
public final class HttpOptions {

    public static final HttpOption<String> HTTP_CLIENT_TYPE = StringHttpOption.builder()
            .key("HTTP_CLIENT_TYPE")
            .propertyKey("solpic.http.client.type")
            .defaultValue("jdk")
            .level(OptionLevel.GLOBAL)
            .build();

    public static final HttpOption<String> HTTP_THREAD_POOL = StringHttpOption.builder()
            .key("HTTP_THREAD_POOL")
            .propertyKey("solpic.http.thread.pool")
            .defaultValue("default")
            .level(OptionLevel.GLOBAL)
            .build();

    public static final HttpOption<String> HTTP_PROTOCOL_VERSION = StringHttpOption.builder()
            .id(1 << 1L)
            .key("HTTP_PROTOCOL_VERSION")
            .propertyKey("solpic.http.protocol.version")
            .defaultValue("1.1")
            .build();

    public static final HttpOption<ProxyConfig> HTTP_PROXY = ProxyHttpOption.builder()
//            .id(1 << 1L)
            .key("HTTP_PROXY")
            .propertyKey("solpic.http.proxy")
            .defaultValue(ProxyConfig.NO)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_LOGGING = BoolHttpOption.builder()
            .id(1 << 2)
            .key("HTTP_ENABLE_LOGGING")
            .propertyKey("solpic.http.enable.logging")
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_EXECUTE_PROFILE = BoolHttpOption.builder()
            .id(1 << 3)
            .key("HTTP_ENABLE_EXECUTE_PROFILE")
            .propertyKey("solpic.http.enable.execute.profile")
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_EXECUTE_TRACING = BoolHttpOption.builder()
            .id(1 << 4)
            .key("HTTP_ENABLE_EXECUTE_TRACING")
            .propertyKey("solpic.http.enable.execute.tracing")
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_FORCE_WRITE = BoolHttpOption.builder()
            .key("HTTP_FORCE_WRITE")
            .propertyKey("solpic.http.force.write")
            .defaultValue(false)
            .build();

    public static List<HttpOption<?>> getMatchedOptions(long opts) {
        List<HttpOption<?>> options = new ArrayList<>();
        for (HttpOption<?> httpOption : OPTIONS) {
            if (httpOption.support(opts)) {
                options.add(httpOption);
            }
        }
        return Collections.unmodifiableList(options);
    }

    private static final List<HttpOption<?>> OPTIONS = new ArrayList<>();

    static {

    }
}
