package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.exception.SolpicHttpException;
import cn.vlts.solpic.core.http.HttpResponse;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default HTTP response.
 *
 * @author throwable
 * @since 2024/7/28 21:17
 */
public class DefaultHttpResponse<T> extends BaseHttpResponse<T> implements HttpResponse<T> {

    private final CompletionStage<T> completionStage;

    private final AtomicBoolean manual = new AtomicBoolean();

    private T manualPayload;

    public DefaultHttpResponse(CompletionStage<T> payloadCompletionStage, int statusCode) {
        this.completionStage = payloadCompletionStage;
        setHttpStatusCode(statusCode);
    }

    @Override
    public T getPayload() {
        try {
            return this.manual.get() ? this.manualPayload : this.completionStage.toCompletableFuture().get();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new SolpicHttpException("Interrupted while getting response payload", interruptedException,
                    ReadOnlyHttpRequest.of(getHttpRequest()));
        } catch (ExecutionException executionException) {
            throw new SolpicHttpException("Failed to get response payload", executionException,
                    ReadOnlyHttpRequest.of(getHttpRequest()));
        }
    }

    public void setPayload(T payload) {
        if (this.manual.compareAndSet(false, true)) {
            this.manualPayload = payload;
        }
    }
}
