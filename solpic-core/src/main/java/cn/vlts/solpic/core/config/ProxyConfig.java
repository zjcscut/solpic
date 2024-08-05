package cn.vlts.solpic.core.config;

import cn.vlts.solpic.core.common.UriScheme;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;
import java.util.Objects;

/**
 * Proxy config.
 *
 * @author throwable
 * @since 2024/7/22 星期一 15:43
 */
@Getter
@EqualsAndHashCode
public class ProxyConfig {

    public static final ProxyConfig NO = new ProxyConfig("", "", -1);

    private final String schema;

    private final String hostname;

    private final int port;

    public ProxyConfig(String schema, String hostname, int port) {
        this.schema = schema;
        this.hostname = hostname;
        this.port = port;
    }

    public static ProxyConfig create(String url) {
        return parse(url);
    }

    private static ProxyConfig parse(String url) {
        URI uri = URI.create(url);
        String schema = Objects.nonNull(uri.getScheme()) ? uri.getScheme() : UriScheme.HTTP.getValue();
        int port = uri.getPort();
        if (port < 0) {
            if (Objects.equals(UriScheme.HTTPS.getValue(), schema)) {
                port = 443;
            } else {
                port = 80;
            }
        }
        return new ProxyConfig(schema, uri.getHost(), port);
    }
}
