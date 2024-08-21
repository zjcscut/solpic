package cn.vlts.solpic.core.config;

import cn.vlts.solpic.core.http.HttpVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Internal HTTP options, id: [1<<0L, 1<<63L].
 *
 * @author throwable
 * @since 2024/7/22 星期一 11:48
 */
public final class HttpOptions {

    // ##################### COMMON OPTIONS #####################

    public static final HttpOption<String> HTTP_CLIENT_TYPE = StringHttpOption.builder()
            .id(1L)
            .key("HTTP_CLIENT_TYPE")
            .propertyKey("solpic.http.client.type")
            .defaultValue("default")
            .level(OptionLevel.GLOBAL)
            .build();

    public static final HttpOption<String> HTTP_CLIENT_ID = StringHttpOption.builder()
            .id(1L << 1)
            .key("HTTP_CLIENT_ID")
            .propertyKey("solpic.http.client.id")
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<HttpVersion> HTTP_PROTOCOL_VERSION = HttpVersionHttpOption.builder()
            .id(1L << 2)
            .key("HTTP_PROTOCOL_VERSION")
            .propertyKey("solpic.http.protocol.version")
            .defaultValue(HttpVersion.HTTP_1_1)
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<String> HTTP_THREAD_POOL = StringHttpOption.builder()
            .id(1L << 3)
            .key("HTTP_THREAD_POOL")
            .propertyKey("solpic.http.thread.pool")
            .defaultValue("default")
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<String> HTTP_SCHEDULED_THREAD_POOL = StringHttpOption.builder()
            .id(1L << 4)
            .key("HTTP_SCHEDULED_THREAD_POOL")
            .propertyKey("solpic.http.scheduled.thread.pool")
            .defaultValue("default")
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<SSLConfig> HTTP_SSL_CONFIG = SSLHttpOption.builder()
            .id(1L << 5)
            .key("HTTP_SSL_CONFIG")
            .defaultValue(SSLConfig.NO)
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<ProxyConfig> HTTP_PROXY = ProxyHttpOption.builder()
            .id(1L << 6)
            .key("HTTP_PROXY")
            .propertyKey("solpic.http.proxy")
            .defaultValue(ProxyConfig.NO)
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_LOGGING = BoolHttpOption.builder()
            .id(1L << 7)
            .key("HTTP_ENABLE_LOGGING")
            .propertyKey("solpic.http.enable.logging")
            .level(OptionLevel.CLIENT)
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_EXECUTE_PROFILE = BoolHttpOption.builder()
            .id(1L << 8)
            .key("HTTP_ENABLE_EXECUTE_PROFILE")
            .propertyKey("solpic.http.enable.execute.profile")
            .level(OptionLevel.CLIENT)
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_ENABLE_EXECUTE_TRACING = BoolHttpOption.builder()
            .id(1L << 9)
            .key("HTTP_ENABLE_EXECUTE_TRACING")
            .propertyKey("solpic.http.enable.execute.tracing")
            .level(OptionLevel.CLIENT)
            .defaultValue(false)
            .build();

    public static final HttpOption<Boolean> HTTP_FORCE_WRITE = BoolHttpOption.builder()
            .id(1L << 10)
            .key("HTTP_FORCE_WRITE")
            .propertyKey("solpic.http.force.write")
            .defaultValue(false)
            .level(OptionLevel.CLIENT)
            .build();

    public static final HttpOption<Boolean> HTTP_RESPONSE_COPY_ATTACHMENTS = BoolHttpOption.builder()
            .id(1L << 11L)
            .key("HTTP_RESPONSE_COPY_ATTACHMENTS")
            .propertyKey("solpic.http.response.copy.attachments")
            .level(OptionLevel.CLIENT)
            .defaultValue(true)
            .build();

    // ##################### CONNECTION POOL OPTIONS #####################

    public static final HttpOption<Boolean> HTTP_CLIENT_ENABLE_CONNECTION_POOL = BoolHttpOption.builder()
            .id(1L << 12)
            .key("HTTP_CLIENT_ENABLE_CONNECTION_POOL")
            .propertyKey("solpic.http.client.enable.connection.pool")
            .level(OptionLevel.CLIENT)
            .defaultValue(true)
            .build();

    public static final HttpOption<Integer> HTTP_CLIENT_CONNECTION_POOL_CAPACITY = IntHttpOption.builder()
            .id(1L << 13)
            .key("HTTP_CLIENT_CONNECTION_POOL_CAPACITY")
            .propertyKey("solpic.http.client.connection.pool.capacity")
            .level(OptionLevel.CLIENT)
            .defaultValue(128)
            .build();

    public static final HttpOption<Integer> HTTP_CLIENT_CONNECTION_TTL = IntHttpOption.builder()
            .id(1L << 14)
            .key("HTTP_CLIENT_CONNECTION_TTL")
            .propertyKey("solpic.http.client.connection.ttl")
            .level(OptionLevel.CLIENT)
            .defaultValue(1800000)
            .build();

    // ##################### OTHER OPTIONS, FOR SPECIFIED CLIENT TYPE #####################

    public static final HttpOption<Integer> HTTP_CONNECT_TIMEOUT = IntHttpOption.builder()
            .id(1L << 15)
            .key("HTTP_CONNECT_TIMEOUT")
            .propertyKey("solpic.http.connect.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_CONNECT_TIMEOUT = IntHttpOption.builder()
            .id(1L << 16)
            .key("HTTP_REQUEST_CONNECT_TIMEOUT")
            .propertyKey("solpic.http.request.connect.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_SOCKET_TIMEOUT = IntHttpOption.builder()
            .id(1L << 17)
            .key("HTTP_SOCKET_TIMEOUT")
            .propertyKey("solpic.http.socket.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_SOCKET_TIMEOUT = IntHttpOption.builder()
            .id(1L << 18)
            .key("HTTP_REQUEST_SOCKET_TIMEOUT")
            .propertyKey("solpic.http.request.socket.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_READ_TIMEOUT = IntHttpOption.builder()
            .id(1L << 19)
            .key("HTTP_READ_TIMEOUT")
            .propertyKey("solpic.http.read.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_WRITE_TIMEOUT = IntHttpOption.builder()
            .id(1L << 20)
            .key("HTTP_WRITE_TIMEOUT")
            .propertyKey("solpic.http.write.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_TIMEOUT = IntHttpOption.builder()
            .id(1L << 21)
            .key("HTTP_TIMEOUT")
            .propertyKey("solpic.http.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_CHUNK_SIZE = IntHttpOption.builder()
            .id(1L << 22)
            .key("HTTP_CHUNK_SIZE")
            .propertyKey("solpic.http.chunk.size")
            .level(OptionLevel.CLIENT)
            .defaultValue(4096)
            .build();

    public static final HttpOption<Boolean> HTTP_REQUEST_FORCE_WRITE = BoolHttpOption.builder()
            .id(1L << 23)
            .key("HTTP_REQUEST_FORCE_WRITE")
            .propertyKey("solpic.http.request.force.write")
            .defaultValue(false)
            .level(OptionLevel.REQUEST)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_CHUNK_SIZE = IntHttpOption.builder()
            .id(1L << 24)
            .key("HTTP_REQUEST_CHUNK_SIZE")
            .propertyKey("solpic.http.request.chunk.size")
            .level(OptionLevel.REQUEST)
            .defaultValue(4096)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_READ_TIMEOUT = IntHttpOption.builder()
            .id(1L << 25)
            .key("HTTP_REQUEST_READ_TIMEOUT")
            .propertyKey("solpic.http.request.read.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_TIMEOUT = IntHttpOption.builder()
            .id(1L << 26)
            .key("HTTP_REQUEST_WRITE_TIMEOUT")
            .propertyKey("solpic.http.request.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_CONNECTION_REQUEST_TIMEOUT = IntHttpOption.builder()
            .id(1L << 27)
            .key("HTTP_CONNECTION_REQUEST_TIMEOUT")
            .propertyKey("solpic.http.connection.request.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_CONNECTION_REQUEST_TIMEOUT = IntHttpOption.builder()
            .id(1L << 28)
            .key("HTTP_CONNECTION_REQUEST_TIMEOUT")
            .propertyKey("solpic.http.request.connection.request.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_RESPONSE_TIMEOUT = IntHttpOption.builder()
            .id(1L << 29)
            .key("HTTP_RESPONSE_TIMEOUT")
            .propertyKey("solpic.http.response.timeout")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_RESPONSE_TIMEOUT = IntHttpOption.builder()
            .id(1L << 30)
            .key("HTTP_REQUEST_RESPONSE_TIMEOUT")
            .propertyKey("solpic.http.request.response.timeout")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_CONNECTION_KEEPALIVE = IntHttpOption.builder()
            .id(1L << 31)
            .key("HTTP_CONNECTION_KEEPALIVE")
            .propertyKey("solpic.http.connection.keepalive")
            .level(OptionLevel.CLIENT)
            .defaultValue(5000)
            .build();

    public static final HttpOption<Integer> HTTP_REQUEST_CONNECTION_KEEPALIVE = IntHttpOption.builder()
            .id(1L << 32)
            .key("HTTP_REQUEST_CONNECTION_KEEPALIVE")
            .propertyKey("solpic.http.request.connection.keepalive")
            .level(OptionLevel.REQUEST)
            .defaultValue(5000)
            .build();

    public static List<HttpOption<?>> getAllOptions() {
        return Collections.unmodifiableList(OPTIONS);
    }

    public static List<HttpOption<?>> getMatchedOptions(long opts) {
        List<HttpOption<?>> options = new ArrayList<>();
        for (HttpOption<?> httpOption : OPTIONS) {
            if (httpOption.support(opts)) {
                options.add(httpOption);
            }
        }
        return Collections.unmodifiableList(options);
    }

    public static List<HttpOption<?>> getOptionsByLevel(OptionLevel level) {
        List<HttpOption<?>> options = new ArrayList<>();
        for (HttpOption<?> httpOption : OPTIONS) {
            if (Objects.equals(httpOption.level(), level)) {
                options.add(httpOption);
            }
        }
        return Collections.unmodifiableList(options);
    }

    public static HttpOption<?> getById(long id) {
        for (HttpOption<?> httpOption : OPTIONS) {
            if (httpOption.id() == id) {
                return httpOption;
            }
        }
        return null;
    }

    public static HttpOption<?> getByKey(String key) {
        for (HttpOption<?> httpOption : OPTIONS) {
            if (Objects.equals(httpOption.key(), key)) {
                return httpOption;
            }
        }
        return null;
    }

    private static final List<HttpOption<?>> OPTIONS = new ArrayList<>();

    static {
        OPTIONS.add(HTTP_CLIENT_TYPE);
        OPTIONS.add(HTTP_CLIENT_ID);
        OPTIONS.add(HTTP_PROTOCOL_VERSION);
        OPTIONS.add(HTTP_THREAD_POOL);
        OPTIONS.add(HTTP_SCHEDULED_THREAD_POOL);
        OPTIONS.add(HTTP_SSL_CONFIG);
        OPTIONS.add(HTTP_PROXY);
        OPTIONS.add(HTTP_ENABLE_LOGGING);
        OPTIONS.add(HTTP_ENABLE_EXECUTE_PROFILE);
        OPTIONS.add(HTTP_ENABLE_EXECUTE_TRACING);
        OPTIONS.add(HTTP_FORCE_WRITE);
        OPTIONS.add(HTTP_RESPONSE_COPY_ATTACHMENTS);
        OPTIONS.add(HTTP_CLIENT_ENABLE_CONNECTION_POOL);
        OPTIONS.add(HTTP_CLIENT_CONNECTION_POOL_CAPACITY);
        OPTIONS.add(HTTP_CLIENT_CONNECTION_TTL);
        OPTIONS.add(HTTP_CONNECT_TIMEOUT);
        OPTIONS.add(HTTP_REQUEST_CONNECT_TIMEOUT);
        OPTIONS.add(HTTP_SOCKET_TIMEOUT);
        OPTIONS.add(HTTP_REQUEST_SOCKET_TIMEOUT);
        OPTIONS.add(HTTP_READ_TIMEOUT);
        OPTIONS.add(HTTP_WRITE_TIMEOUT);
        OPTIONS.add(HTTP_TIMEOUT);
        OPTIONS.add(HTTP_CHUNK_SIZE);
        OPTIONS.add(HTTP_REQUEST_FORCE_WRITE);
        OPTIONS.add(HTTP_REQUEST_CHUNK_SIZE);
        OPTIONS.add(HTTP_REQUEST_READ_TIMEOUT);
        OPTIONS.add(HTTP_REQUEST_TIMEOUT);
        OPTIONS.add(HTTP_CONNECTION_REQUEST_TIMEOUT);
        OPTIONS.add(HTTP_REQUEST_CONNECTION_REQUEST_TIMEOUT);
        OPTIONS.add(HTTP_RESPONSE_TIMEOUT);
        OPTIONS.add(HTTP_REQUEST_RESPONSE_TIMEOUT);
        OPTIONS.add(HTTP_CONNECTION_KEEPALIVE);
        OPTIONS.add(HTTP_REQUEST_CONNECTION_KEEPALIVE);
    }
}
