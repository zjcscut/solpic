package cn.vlts.solpic.core.config;

import cn.vlts.solpic.core.http.support.ExecuteProfiler;

/**
 * Internal HTTP options.
 *
 * @author throwable
 * @since 2024/7/22 星期一 11:48
 */
public final class HttpOptions {

    public static final HttpOption<String> HTTP_CLIENT_TYPE = StringHttpOption.builder()
            .id(1L)
            .key("HTTP_CLIENT_TYPE")
            .propertyKey("solpic.http.client.type")
            .defaultValue("jdk")
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


    public static final HttpOption<ExecuteProfiler> HTTP_EXECUTE_PROFILER = ExecuteProfilerHttpOption.builder()
//            .id(1 << 1L)
            .key("HTTP_EXECUTE_PROFILER")
            .propertyKey("solpic.http.execute.profiler")
            .defaultValue(ExecuteProfiler.NO_OP)
            .build();
}
