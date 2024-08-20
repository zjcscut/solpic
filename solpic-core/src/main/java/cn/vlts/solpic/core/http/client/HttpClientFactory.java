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

    public HttpClient loadHttpClient(String httpClientName) {
        if (Objects.nonNull(httpClientName)) {
            if (getSpiLoader().getAvailableServiceNames().contains(httpClientName)) {
                return getSpiLoader().getService(httpClientName);
            }
        }
        HttpClient httpClient = loadBestMatchedHttpClient();
        if (Objects.nonNull(httpClient)) {
            return httpClient;
        }
        throw new IllegalArgumentException("Failed to load httpClient");
    }

    public HttpClient loadBestMatchedHttpClient() {
        return getSpiLoader()
                .getAvailableServiceNames()
                .stream()
                .findFirst()
                .map(serviceName -> getSpiLoader().getService(serviceName))
                .orElseThrow(() -> new IllegalStateException("Unable to load any available HttpClient"));
    }

    private SpiLoader<HttpClient> getSpiLoader() {
        if (Objects.isNull(spiLoader)) {
            synchronized (this) {
                if (Objects.isNull(spiLoader)) {
                    spiLoader = SpiLoader.getSpiLoader(HttpClient.class);
                }
            }
        }
        return spiLoader;
    }
}
