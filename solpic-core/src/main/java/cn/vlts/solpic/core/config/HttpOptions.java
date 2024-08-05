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

    public static final HttpOption<String> HTTP_SCHEDULED_THREAD_POOL = StringHttpOption.builder()
            .key("HTTP_SCHEDULED_THREAD_POOL")
            .propertyKey("solpic.http.scheduled.thread.pool")
            .defaultValue("default")
            .level(OptionLevel.GLOBAL)
            .build();

    public static final HttpOption<String> HTTP_CLIENT_ID = StringHttpOption.builder()
            .key("HTTP_CLIENT_ID")
            .propertyKey("solpic.http.client.id")
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<String> HTTP_PROTOCOL_VERSION = StringHttpOption.builder()
            .id(1 << 1L)
            .key("HTTP_PROTOCOL_VERSION")
            .propertyKey("solpic.http.protocol.version")
            .defaultValue("1.1")
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<ProxyConfig> HTTP_PROXY = ProxyHttpOption.builder()
//            .id(1 << 1L)
            .key("HTTP_PROXY")
            .propertyKey("solpic.http.proxy")
            .defaultValue(ProxyConfig.NO)
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<Boolean> HTTP_CLIENT_ENABLE_CONNECTION_POOL = BoolHttpOption.builder()
            .key("HTTP_CLIENT_ENABLE_CONNECTION_POOL")
            .propertyKey("solpic.http.client.enable.connection.pool")
            .level(OptionLevel.CLIENT)
            .defaultValue(false)
            .build();

    public static final HttpOption<Integer> HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE = IntHttpOption.builder()
            .key("HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE")
            .propertyKey("solpic.http.client.connection.pool.max.size")
            .level(OptionLevel.CLIENT)
            .defaultValue(20)
            .build();

    public static final HttpOption<Integer> HTTP_CLIENT_CONNECTION_POOL_TTL = IntHttpOption.builder()
            .key("HTTP_CLIENT_CONNECTION_POOL_TTL")
            .propertyKey("solpic.http.client.connection.pool.ttl")
            .level(OptionLevel.CLIENT)
            .defaultValue(1800000)
            .build();

    public static final HttpOption<Integer> HTTP_CONNECT_TIMEOUT = IntHttpOption.builder()
            .key("HTTP_CONNECT_TIMEOUT")
            .propertyKey("solpic.http.connect.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_READ_TIMEOUT = IntHttpOption.builder()
            .key("HTTP_READ_TIMEOUT")
            .propertyKey("solpic.http.read.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_WRITE_TIMEOUT = IntHttpOption.builder()
            .key("HTTP_WRITE_TIMEOUT")
            .propertyKey("solpic.http.write.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_CHUNK_SIZE = IntHttpOption.builder()
            .key("HTTP_CHUNK_SIZE")
            .propertyKey("solpic.http.chunk.size")
            .level(OptionLevel.CLIENT)
            .defaultValue(4096)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_LOGGING = BoolHttpOption.builder()
            .id(1 << 2)
            .key("HTTP_ENABLE_LOGGING")
            .propertyKey("solpic.http.enable.logging")
            .level(OptionLevel.CLIENT)
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_EXECUTE_PROFILE = BoolHttpOption.builder()
            .id(1 << 3)
            .key("HTTP_ENABLE_EXECUTE_PROFILE")
            .propertyKey("solpic.http.enable.execute.profile")
            .level(OptionLevel.CLIENT)
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_EXECUTE_TRACING = BoolHttpOption.builder()
            .id(1 << 4)
            .key("HTTP_ENABLE_EXECUTE_TRACING")
            .propertyKey("solpic.http.enable.execute.tracing")
            .level(OptionLevel.CLIENT)
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_FORCE_WRITE = BoolHttpOption.builder()
            .key("HTTP_FORCE_WRITE")
            .propertyKey("solpic.http.force.write")
            .defaultValue(false)
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<Boolean> HTTP_RESPONSE_COPY_ATTACHMENTS = BoolHttpOption.builder()
            .id(1 << 5)
            .key("HTTP_RESPONSE_COPY_ATTACHMENTS")
            .propertyKey("solpic.http.response.copy.attachments")
            .level(OptionLevel.CLIENT)
            .defaultValue(true)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_CHUNK_SIZE = IntHttpOption.builder()
            .key("HTTP_REQUEST_CHUNK_SIZE")
            .propertyKey("solpic.http.request.chunk.size")
            .level(OptionLevel.REQUEST)
            .defaultValue(4096)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_CONNECT_TIMEOUT = IntHttpOption.builder()
            .key("HTTP_REQUEST_CONNECT_TIMEOUT")
            .propertyKey("solpic.http.request.connect.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_READ_TIMEOUT = IntHttpOption.builder()
            .key("HTTP_REQUEST_READ_TIMEOUT")
            .propertyKey("solpic.http.request.read.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_WRITE_TIMEOUT = IntHttpOption.builder()
            .key("HTTP_REQUEST_WRITE_TIMEOUT")
            .propertyKey("solpic.http.request.write.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
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
