package cn.vlts.solpic.core.flow;

/**
 * See JDK9 Flow.Subscriber.
 *
 * @author throwable
 * @since 2024/7/24 00:00
 */
public interface Subscriber<T> {

    void onSubscribe(Subscription subscription);

    void onNext(T item);

    void onError(Throwable throwable);

    void onComplete();
}
