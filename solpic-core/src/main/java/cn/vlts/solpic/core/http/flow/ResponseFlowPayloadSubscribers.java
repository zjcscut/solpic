package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.MinimalFuture;
import cn.vlts.solpic.core.flow.Publisher;
import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.util.IoUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Response flow payload subscribers.
 *
 * @author throwable
 * @since 2024/7/31 星期三 15:33
 */
public class ResponseFlowPayloadSubscribers {

    private ResponseFlowPayloadSubscribers() {
        throw new Error();
    }

    public static class ByteArrayConsumerFlowPayloadSubscriber implements FlowPayloadSubscriber<Void> {

        private final Consumer<Optional<byte[]>> consumer;

        private Subscription subscription;

        private final CompletableFuture<Void> result = new MinimalFuture<>();

        private final AtomicBoolean subscribed = new AtomicBoolean();

        public ByteArrayConsumerFlowPayloadSubscriber(Consumer<Optional<byte[]>> consumer) {
            this.consumer = consumer;
        }

        @Override
        public CompletionStage<Void> getPayload() {
            return result;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            if (!subscribed.compareAndSet(false, true)) {
                subscription.cancel();
            } else {
                this.subscription = subscription;
                subscription.request(1);
            }
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            for (ByteBuffer byteBuffer : item) {
                if (byteBuffer.hasRemaining()) {
                    byte[] buf = new byte[byteBuffer.remaining()];
                    byteBuffer.get(buf);
                    consumer.accept(Optional.of(buf));
                }
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            result.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            consumer.accept(Optional.empty());
            result.complete(null);
        }
    }

    public static class ByteArrayFlowPayloadSubscriber<T> implements FlowPayloadSubscriber<T> {

        private final Function<byte[], T> finisher;

        private final CompletableFuture<T> result = new MinimalFuture<>();

        private final List<ByteBuffer> received = new ArrayList<>();

        private volatile Subscription subscription;

        public ByteArrayFlowPayloadSubscriber(Function<byte[], T> finisher) {
            this.finisher = finisher;
        }

        @Override
        public CompletionStage<T> getPayload() {
            return result;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            if (Objects.nonNull(this.subscription)) {
                subscription.cancel();
                return;
            }
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            received.addAll(item);
        }

        @Override
        public void onError(Throwable throwable) {
            received.clear();
            result.completeExceptionally(throwable);
        }

        @Override
        public void onComplete() {
            try {
                result.complete(finisher.apply(IoUtils.X.copyByteBuffersToByteArray(received)));
                received.clear();
            } catch (Throwable throwable) {
                result.completeExceptionally(throwable);
            }
        }
    }

    public static class FlowPayloadSubscriberAdapter<S extends Subscriber<List<ByteBuffer>>, T>
            implements FlowPayloadSubscriber<T> {

        private volatile Subscription subscription;

        private final CompletableFuture<T> result = new MinimalFuture<>();

        private final Function<? super S, ? extends T> finisher;

        private final S subscriber;

        public FlowPayloadSubscriberAdapter(Function<? super S, ? extends T> finisher, S subscriber) {
            this.finisher = finisher;
            this.subscriber = subscriber;
        }

        @Override
        public CompletionStage<T> getPayload() {
            return result;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            if (Objects.nonNull(this.subscription)) {
                subscription.cancel();
            } else {
                this.subscription = subscription;
                subscriber.onSubscribe(subscription);
            }
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            try {
                subscriber.onNext(item);
            } catch (Throwable throwable) {
                subscription.cancel();
                result.completeExceptionally(throwable);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            try {
                subscriber.onError(throwable);
            } finally {
                result.completeExceptionally(throwable);
            }
        }

        @Override
        public void onComplete() {
            try {
                subscriber.onComplete();
            } finally {
                try {
                    result.complete(finisher.apply(subscriber));
                } catch (Throwable throwable) {
                    result.completeExceptionally(throwable);
                }
            }
        }
    }

    public static class MappingFlowPayloadSubscriber<U, T> implements FlowPayloadSubscriber<T> {

        private final FlowPayloadSubscriber<U> upstream;

        private final Function<? super U, ? extends T> mapper;

        public MappingFlowPayloadSubscriber(FlowPayloadSubscriber<U> upstream, Function<? super U, ? extends T> mapper) {
            this.upstream = upstream;
            this.mapper = mapper;
        }

        @Override
        public CompletionStage<T> getPayload() {
            return upstream.getPayload().thenApply(mapper);
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            upstream.onSubscribe(subscription);
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
            upstream.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
            upstream.onError(throwable);
        }

        @Override
        public void onComplete() {
            upstream.onComplete();
        }
    }
}
