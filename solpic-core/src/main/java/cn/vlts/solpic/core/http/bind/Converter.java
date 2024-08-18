package cn.vlts.solpic.core.http.bind;

/**
 * Converter.
 *
 * @author throwable
 * @since 2024/8/17 18:07
 */
@FunctionalInterface
public interface Converter<S, T> {

    T convert(S source);
}
