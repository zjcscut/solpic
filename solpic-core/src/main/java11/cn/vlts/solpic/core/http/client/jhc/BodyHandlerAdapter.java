package cn.vlts.solpic.core.http.client.jhc;

import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.http.PayloadSubscriber;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.flow.PullingInputStream;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BodyHandler adapter.
 *
 * @author throwable
 * @since 2024/8/7 星期三 11:39
 */
public class BodyHandlerAdapter<T> implements HttpResponse.BodyHandler<T> {

    private final ResponsePayloadSupport<T> responsePayloadSupport;

    public static <T> BodyHandlerAdapter<T> newInstance(ResponsePayloadSupport<T> responsePayloadSupport) {
        return new BodyHandlerAdapter<>(responsePayloadSupport);
    }

    private BodyHandlerAdapter(ResponsePayloadSupport<T> responsePayloadSupport) {
        this.responsePayloadSupport = responsePayloadSupport;
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(HttpResponse.ResponseInfo responseInfo) {
        if (responsePayloadSupport instanceof PayloadSubscriber) {
            PayloadSubscriber<T> subscriber = (PayloadSubscriber<T>) responsePayloadSupport;
            return new HttpResponse.BodySubscriber<>() {

                private PullingInputStream pullingInputStream;

                private final AtomicBoolean subscribed = new AtomicBoolean(false);

                @Override
                public CompletionStage<T> getBody() {
                    return subscriber.getPayload();
                }

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    if (!subscribed.compareAndSet(false, true)) {
                        subscription.cancel();
                        return;
                    }
                    pullingInputStream = new PullingInputStream(new Subscription() {
                        @Override
                        public void request(long n) {
                            subscription.request(n);
                        }

                        @Override
                        public void cancel() {
                            subscription.cancel();
                        }
                    });
                    subscription.request(1);
                }

                @Override
                public void onNext(List<ByteBuffer> item) {
                    for (ByteBuffer buffer : item) {
                        try {
                            pullingInputStream.offer(buffer);
                        } catch (IOException e) {
                            onError(e);
                        }
                    }
                    pullingInputStream.tryRequestMore();
                }

                @Override
                public void onError(Throwable throwable) {
                    IoUtils.X.closeQuietly(pullingInputStream);
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onComplete() {
                    try {
                        subscriber.readFrom(pullingInputStream);
                    } catch (IOException e) {
                        onError(e);
                    } finally {
                        IoUtils.X.closeQuietly(pullingInputStream);
                    }
                }
            };
        } else if (responsePayloadSupport instanceof FlowPayloadSubscriber) {
            FlowPayloadSubscriber<T> flowSubscriber = (FlowPayloadSubscriber<T>) responsePayloadSupport;
            return new HttpResponse.BodySubscriber<>() {

                private final AtomicBoolean subscribed = new AtomicBoolean(false);

                @Override
                public CompletionStage<T> getBody() {
                    return flowSubscriber.getPayload();
                }

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    if (!subscribed.compareAndSet(false, true)) {
                        subscription.cancel();
                        return;
                    }
                    flowSubscriber.onSubscribe(new Subscription() {
                        @Override
                        public void request(long n) {
                            subscription.request(n);
                        }

                        @Override
                        public void cancel() {
                            subscription.cancel();
                        }
                    });
                    subscription.request(1);
                }

                @Override
                public void onNext(List<ByteBuffer> item) {
                    flowSubscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    flowSubscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    flowSubscriber.onComplete();
                }
            };
        }
        throw new IllegalStateException("Convert to BodySubscriber failed");
    }
}
