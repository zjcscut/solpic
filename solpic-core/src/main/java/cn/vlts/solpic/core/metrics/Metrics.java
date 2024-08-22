package cn.vlts.solpic.core.metrics;

import cn.vlts.solpic.core.common.HttpStatusSeries;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Metrics.
 *
 * @author throwable
 * @since 2024/7/27 21:19
 */
enum Metrics {
    X;

    private static final ConcurrentMap<String, HttpClientStats> CLIENT_CACHE = new ConcurrentHashMap<>();

    public void initHttpClientStats(String id) {
        getOrCreateHttpClientStats(id);
    }

    public HttpClientStats getOrCreateHttpClientStats(String id) {
        return CLIENT_CACHE.computeIfAbsent(id, HttpClientStats::newInstance);
    }

    public HttpClientStats getHttpClientStats(String id) {
        return CLIENT_CACHE.get(id);
    }

    public void increaseTotalRequestCount(String id) {
        Optional.ofNullable(getOrCreateHttpClientStats(id))
                .ifPresent(httpClientStats -> {
                    httpClientStats.increment(StatsFactorType.TOTAL_REQUEST_COUNT);
                    httpClientStats.increment(StatsFactorType.ACTIVE_REQUEST_COUNT);
                });
    }

    public void increaseFailedRequestCount(String id) {
        Optional.ofNullable(getOrCreateHttpClientStats(id))
                .ifPresent(httpClientStats -> {
                    httpClientStats.increment(StatsFactorType.FAILED_REQUEST_COUNT);
                    httpClientStats.decrement(StatsFactorType.ACTIVE_REQUEST_COUNT);
                });
    }

    public void increaseCompletedRequestCount(String id) {
        Optional.ofNullable(getOrCreateHttpClientStats(id))
                .ifPresent(httpClientStats -> {
                    httpClientStats.increment(StatsFactorType.COMPLETED_REQUEST_COUNT);
                    httpClientStats.decrement(StatsFactorType.ACTIVE_REQUEST_COUNT);
                });
    }

    public void increaseHttpStatusSeriesCount(String id, HttpStatusSeries series) {
        Optional.ofNullable(getOrCreateHttpClientStats(id))
                .ifPresent(httpClientStats -> httpClientStats.incrementSeries(series));
    }

    public void removeHttpClientStats(String id) {
        CLIENT_CACHE.remove(id);
    }
}
