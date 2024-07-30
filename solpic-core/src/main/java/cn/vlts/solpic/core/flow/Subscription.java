package cn.vlts.solpic.core.flow;

/**
 * See JDK9+ Flow.Subscription.
 *
 * @author throwable
 * @since 2024/7/24 00:01
 */
public interface Subscription {

    void request(long n);

    void cancel();
}
