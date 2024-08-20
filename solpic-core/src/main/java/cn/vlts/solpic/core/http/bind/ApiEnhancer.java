package cn.vlts.solpic.core.http.bind;

/**
 * Api enhancer.
 *
 * @author throwable
 * @since 2024/8/21 00:07
 */
@FunctionalInterface
public interface ApiEnhancer {

    <T> T enhance(Class<T> type);
}
