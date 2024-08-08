package cn.vlts.solpic.core.metrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Stats factor info.
 *
 * @author throwable
 * @since 2024/8/8 星期四 15:47
 */
@RequiredArgsConstructor
@Getter
public class StatsFactorInfo {

    private final String name;

    private final long value;
}
