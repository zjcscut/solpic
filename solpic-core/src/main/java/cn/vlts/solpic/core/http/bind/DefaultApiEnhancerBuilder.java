package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.client.HttpClientFactory;
import cn.vlts.solpic.core.util.ArgumentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Default api enhancer builder.
 *
 * @author throwable
 * @since 2024/8/14 23:54
 */
@SuppressWarnings("rawtypes")
class DefaultApiEnhancerBuilder implements ApiEnhancerBuilder {

    private boolean loadEagerly = false;

    private String baseUrl;

    private HttpClient httpClient;

    private Codec codec;

    private Long delay = 0L;

    private Supplier<CompletableFuture> promise = CompletableFuture::new;

    private Supplier<FutureListener> listener = () -> f -> {
    };

    private final List<ConverterFactory> converterFactories = new ArrayList<>();

    @Override
    public ApiEnhancerBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public ApiEnhancerBuilder loadEagerly() {
        this.loadEagerly = true;
        return this;
    }

    @Override
    public ApiEnhancerBuilder delay(long delay) {
        ArgumentUtils.X.isTrue(delay > 0, "delay must be greater than 0");
        this.delay = delay;
        return this;
    }

    @Override
    public ApiEnhancerBuilder promise(Supplier<CompletableFuture> promise) {
        ArgumentUtils.X.notNull("promise", promise);
        this.promise = promise;
        return this;
    }

    @Override
    public ApiEnhancerBuilder listener(Supplier<FutureListener> listener) {
        ArgumentUtils.X.notNull("listener", listener);
        this.listener = listener;
        return this;
    }

    @Override
    public ApiEnhancerBuilder httpClient(HttpClient httpClient) {
        ArgumentUtils.X.notNull("httpClient", httpClient);
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public ApiEnhancerBuilder codec(Codec codec) {
        ArgumentUtils.X.notNull("codec", codec);
        this.codec = codec;
        return this;
    }

    @Override
    public ApiEnhancerBuilder addConverterFactory(ConverterFactory converterFactory) {
        ArgumentUtils.X.notNull("converterFactory", converterFactory);
        converterFactories.add(converterFactory);
        return this;
    }

    @Override
    public ApiEnhancer build() {
        ArgumentUtils.X.notNull("baseUrl", baseUrl);
        if (Objects.isNull(httpClient)) {
            httpClient = HttpClientFactory.X.loadBestMatchedHttpClient();
        }
        ArgumentUtils.X.notNull("httpClient", httpClient);
        return new DefaultApiEnhancer(
                loadEagerly,
                baseUrl,
                httpClient,
                codec,
                delay,
                promise,
                listener,
                converterFactories
        );
    }
}
