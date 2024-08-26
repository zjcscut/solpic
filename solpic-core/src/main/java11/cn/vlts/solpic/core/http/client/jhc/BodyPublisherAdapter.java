package cn.vlts.solpic.core.http.client.jhc;

import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.http.RequestPayloadSupport;
import cn.vlts.solpic.core.http.flow.ByteBufferConsumerOutputStream;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublisher;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

/**
 * BodyPublisher adapter.
 *
 * @author throwable
 * @since 2024/8/7 星期三 11:23
 */
public class BodyPublisherAdapter implements HttpRequest.BodyPublisher {

    private final RequestPayloadSupport requestPayloadSupport;

    public static BodyPublisherAdapter newInstance(RequestPayloadSupport requestPayloadSupport) {
        return new BodyPublisherAdapter(requestPayloadSupport);
    }

    private BodyPublisherAdapter(RequestPayloadSupport requestPayloadSupport) {
        this.requestPayloadSupport = requestPayloadSupport;
    }

    @Override
    public long contentLength() {
        return requestPayloadSupport.contentLength();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        if (requestPayloadSupport instanceof PayloadPublisher) {
            PayloadPublisher publisher = (PayloadPublisher) requestPayloadSupport;
            ByteBufferConsumerOutputStream outputStream = new ByteBufferConsumerOutputStream(subscriber::onNext);
            Flow.Subscription subscription = new Flow.Subscription() {
                @Override
                public void request(long n) {
                    if (n > 0) {
                        boolean success = true;
                        try {
                            publisher.writeTo(outputStream);
                        } catch (IOException e) {
                            success = false;
                            subscriber.onError(e);
                        } finally {
                            IoUtils.X.closeQuietly(outputStream);
                            if (success) {
                                subscriber.onComplete();
                            }
                        }
                    }
                }

                @Override
                public void cancel() {
                    // no-op
                }
            };
            subscriber.onSubscribe(subscription);
        } else if (requestPayloadSupport instanceof FlowPayloadPublisher) {
            FlowPayloadPublisher flowPublisher = (FlowPayloadPublisher) requestPayloadSupport;
            flowPublisher.subscribe(new Subscriber<>() {
                @Override
                public void onSubscribe(Subscription subscription) {
                    subscriber.onSubscribe(new Flow.Subscription() {
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
                public void onNext(ByteBuffer item) {
                    subscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    subscriber.onComplete();
                }
            });
        }
    }
}
