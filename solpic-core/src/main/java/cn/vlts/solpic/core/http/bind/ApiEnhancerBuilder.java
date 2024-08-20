package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.http.HttpClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Api enhancer builder.
 *
 * @author throwable
 * @since 2024/8/14 23:55
 */
@SuppressWarnings("rawtypes")
public interface ApiEnhancerBuilder {

    ApiEnhancerBuilder baseUrl(String baseUrl);

    ApiEnhancerBuilder loadEagerly();

    ApiEnhancerBuilder delay(long delay);

    ApiEnhancerBuilder promise(Supplier<CompletableFuture> promise);

    ApiEnhancerBuilder listener(Supplier<FutureListener> listener);

    ApiEnhancerBuilder httpClient(HttpClient httpClient);

    ApiEnhancerBuilder codec(Codec codec);

    ApiEnhancerBuilder addConverterFactory(ConverterFactory converterFactory);

    ApiEnhancer build();

    static ApiEnhancerBuilder newBuilder() {
        return new DefaultApiEnhancerBuilder();
    }
}
