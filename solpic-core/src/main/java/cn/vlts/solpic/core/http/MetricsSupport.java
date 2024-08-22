package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.metrics.StatsFactorInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Metrics support.
 *
 * @author throwable
 * @since 2024/8/23 01:03
 */
public interface MetricsSupport {

    LocalDateTime getLoadTime();

    Duration getUpDuration();

    void consumeStats(Consumer<StatsFactorInfo> consumer);

    List<StatsFactorInfo> getStats();
}
