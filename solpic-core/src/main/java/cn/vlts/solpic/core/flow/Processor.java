package cn.vlts.solpic.core.flow;

/**
 * See JDK9 Flow.Processor.
 *
 * @author throwable
 * @since 2024/7/24 00:02
 */
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {

}
