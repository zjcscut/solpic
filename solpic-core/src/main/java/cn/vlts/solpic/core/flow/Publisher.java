package cn.vlts.solpic.core.flow;

/**
 * See JDK9+ Flow.Publisher.
 *
 * @author throwable
 * @since 2024/7/24 00:03
 */
@FunctionalInterface
public interface Publisher<T> {

    void subscribe(Subscriber<? super T> subscriber);
}
