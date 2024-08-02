package cn.vlts.solpic.core.http.flow;

import cn.vlts.solpic.core.flow.Subscriber;
import cn.vlts.solpic.core.flow.Subscription;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Flow OutputStream subscriber.
 *
 * @author throwable
 * @since 2024/8/2 星期五 15:46
 */
public class FlowOutputStreamSubscriber implements Subscriber<ByteBuffer> {

    private final OutputStream outputStream;

    private Subscription subscription;

    public static FlowOutputStreamSubscriber ofOutputStream(OutputStream outputStream) {
        return new FlowOutputStreamSubscriber(outputStream);
    }

    private FlowOutputStreamSubscriber(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        if (Objects.nonNull(this.subscription)) {
            subscription.cancel();
        } else {
            this.subscription = subscription;
            subscription.request(1);
        }
    }

    @Override
    public void onNext(ByteBuffer item) {
        if (item.hasRemaining()) {
            try {
                while (item.hasRemaining()) {
                    outputStream.write(item.get());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
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
            outputStream.flush();
        } catch (IOException ignore) {

        }
        IoUtils.X.closeQuietly(outputStream);
    }
}
