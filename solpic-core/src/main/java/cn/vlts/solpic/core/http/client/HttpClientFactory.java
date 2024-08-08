package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.spi.SpiLoader;

import java.util.Objects;

/**
 * HttpClient Factory.
 *
 * @author throwable
 * @since 2024/8/8 星期四 20:40
 */
public enum HttpClientFactory {
    X;

    private volatile SpiLoader<HttpClient> spiLoader;

    public HttpClient loadBestMatchedHttpClient() {
        return getSpiLoader()
                .getAvailableServiceNames()
                .stream()
                .findFirst()
                .map(serviceName -> getSpiLoader().getService(serviceName))
                .orElse(null);
    }

    private SpiLoader<HttpClient> getSpiLoader() {
        if (Objects.isNull(spiLoader)) {
            synchronized (this) {
                spiLoader = SpiLoader.getSpiLoader(HttpClient.class);
            }
        }
        return spiLoader;
    }
}
