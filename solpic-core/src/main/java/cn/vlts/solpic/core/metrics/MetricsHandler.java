package cn.vlts.solpic.core.metrics;

import cn.vlts.solpic.core.common.HttpStatusSeries;
import cn.vlts.solpic.core.util.ArgumentUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Metrics handler.
 *
 * @author throwable
 * @since 2024/8/23 00:34
 */
public interface MetricsHandler {

    MetricsHandler NONE = new MetricsHandler() {
    };

    MetricsHandler DEFAULT = new DefaultMetricsHandler();

    default void increaseTotalRequestCount(String clientId) {

    }

    default void increaseFailedRequestCount(String clientId) {

    }

    default void increaseCompletedRequestCount(String clientId) {

    }

    default void increaseHttpStatusSeriesCount(String clientId, HttpStatusSeries series) {

    }

    default LocalDateTime getLoadTime(String clientId) {
        return null;
    }

    default Duration getUpDuration(String clientId) {
        return null;
    }

    default void consumeStats(String clientId, Consumer<StatsFactorInfo> consumer) {

    }

    default List<StatsFactorInfo> getStats(String clientId) {
        return null;
    }

    default void reset(String clientId) {

    }

    class DefaultMetricsHandler implements MetricsHandler {

        @Override
        public void increaseTotalRequestCount(String clientId) {
            Metrics.X.increaseTotalRequestCount(clientId);
        }

        @Override
        public void increaseFailedRequestCount(String clientId) {
            Metrics.X.increaseFailedRequestCount(clientId);
        }

        @Override
        public void increaseCompletedRequestCount(String clientId) {
            Metrics.X.increaseCompletedRequestCount(clientId);
        }

        @Override
        public void increaseHttpStatusSeriesCount(String clientId, HttpStatusSeries series) {
            Metrics.X.increaseHttpStatusSeriesCount(clientId, series);
        }

        @Override
        public LocalDateTime getLoadTime(String clientId) {
            return Optional.ofNullable(Metrics.X.getOrCreateHttpClientStats(clientId))
                    .map(HttpClientStats::getLoadTime)
                    .orElse(null);
        }

        @Override
        public Duration getUpDuration(String clientId) {
            return Optional.ofNullable(Metrics.X.getOrCreateHttpClientStats(clientId))
                    .map(HttpClientStats::getUpDuration)
                    .orElse(null);
        }

        @Override
        public void consumeStats(String clientId, Consumer<StatsFactorInfo> consumer) {
            ArgumentUtils.X.notNull("consumer", consumer);
            Optional.ofNullable(Metrics.X.getOrCreateHttpClientStats(clientId))
                    .ifPresent(httpClientStats -> httpClientStats.consume(consumer));
        }

        @Override
        public List<StatsFactorInfo> getStats(String clientId) {
            return Optional.ofNullable(Metrics.X.getOrCreateHttpClientStats(clientId))
                    .map(HttpClientStats::getStatsFactors)
                    .orElse(null);
        }

        @Override
        public void reset(String clientId) {
            Metrics.X.removeHttpClientStats(clientId);
        }
    }
}
