package cn.vlts.solpic.core.metrics;

import cn.vlts.solpic.core.common.HttpStatusSeries;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * HTTP client stats.
 *
 * @author throwable
 * @since 2024/8/8 星期四 10:53
 */
class HttpClientStats {

    private final String id;

    private final LocalDateTime loadTime;

    private final ConcurrentMap<StatsFactorType, StatsFactor> statsFactors = new ConcurrentHashMap<>();

    private final ConcurrentMap<HttpStatusSeries, StatsFactor> seriesFactors = new ConcurrentHashMap<>();

    static HttpClientStats newInstance(String id, LocalDateTime loadTime) {
        return new HttpClientStats(id, loadTime);
    }

    static HttpClientStats newInstance(String id) {
        return new HttpClientStats(id, LocalDateTime.now());
    }

    private HttpClientStats(String id, LocalDateTime loadTime) {
        this.id = id;
        this.loadTime = loadTime;
        initFactors();
    }

    String getId() {
        return id;
    }

    LocalDateTime getLoadTime() {
        return loadTime;
    }

    Duration getUpDuration() {
        return Duration.between(loadTime, LocalDateTime.now());
    }

    void increment(StatsFactorType factorType) {
        Optional.ofNullable(statsFactors.get(factorType)).ifPresent(StatsFactor::increment);
    }

    void decrement(StatsFactorType factorType) {
        Optional.ofNullable(statsFactors.get(factorType)).ifPresent(StatsFactor::decrement);
    }

    void incrementSeries(HttpStatusSeries series) {
        Optional.ofNullable(seriesFactors.get(series)).ifPresent(StatsFactor::increment);
    }

    void consume(Consumer<StatsFactorInfo> consumer) {
        getStatsFactors().forEach(consumer);
    }

    List<StatsFactorInfo> getStatsFactors() {
        List<StatsFactorInfo> list = new ArrayList<>();
        statsFactors.forEach((k, v) -> list.add(new StatsFactorInfo(k.name(), v.getValue())));
        seriesFactors.forEach((k, v) -> list.add(new StatsFactorInfo(k.name(), v.getValue())));
        return list;
    }

    public void reset() {
        synchronized (this) {
            statsFactors.clear();
            seriesFactors.clear();
            initFactors();
        }
    }

    private void initFactors() {
        for (StatsFactorType factorType : StatsFactorType.values()) {
            statsFactors.put(factorType, new StatsFactor());
        }
        for (HttpStatusSeries series : HttpStatusSeries.values()) {
            seriesFactors.put(series, new StatsFactor());
        }
    }
}
