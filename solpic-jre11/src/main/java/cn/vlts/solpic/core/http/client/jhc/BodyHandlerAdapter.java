package cn.vlts.solpic.core.http.client.jhc;

import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.http.PayloadSubscriber;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscriber;
import cn.vlts.solpic.core.http.flow.PullingInputStream;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

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
            Flow.Subscription nop = new Flow.Subscription() {
                @Override
                public void request(long n) {

                }

                @Override
                public void cancel() {

                }
            };
            Subscription bodySubscriberSubscription = new Subscription() {
                @Override
                public void request(long n) {
                    nop.request(n);
                }

                @Override
                public void cancel() {
                    nop.cancel();
                }
            };
            PullingInputStream pullingInputStream = new PullingInputStream(bodySubscriberSubscription);
            PayloadSubscriber<T> subscriber = (PayloadSubscriber<T>) responsePayloadSupport;
            return new HttpResponse.BodySubscriber<>() {
                @Override
                public CompletionStage<T> getBody() {
                    return subscriber.getPayload();
                }

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
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
                }

                @Override
                public void onError(Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }

                @Override
                public void onComplete() {
                    try {
                        subscriber.readFrom(pullingInputStream);
                    } catch (IOException e) {
                        onError(e);
                    }
                }
            };
        } else if (responsePayloadSupport instanceof FlowPayloadSubscriber) {
            FlowPayloadSubscriber<T> flowSubscriber = (FlowPayloadSubscriber<T>) responsePayloadSupport;
            return new HttpResponse.BodySubscriber<>() {
                @Override
                public CompletionStage<T> getBody() {
                    return flowSubscriber.getPayload();
                }

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
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
