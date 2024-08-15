package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.spi.Spi;

/**
 * Api service enhancer.
 *
 * @author throwable
 * @since 2024/8/15 星期四 9:29
 */
@Spi(value = ApiServiceEnhancer.DEFAULT)
public interface ApiServiceEnhancer {

    String DEFAULT = "default";

    <T> T enhance(Class<T> type);
}
