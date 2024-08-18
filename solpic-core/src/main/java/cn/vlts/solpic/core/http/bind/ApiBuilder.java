package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.http.HttpClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Api builder.
 *
 * @author throwable
 * @since 2024/8/14 23:55
 */
public interface ApiBuilder {

    ApiBuilder baseUrl(String baseUrl);

    ApiBuilder loadEagerly();

    ApiBuilder delay(long delay);

    ApiBuilder promise(Supplier<CompletableFuture> promise);

    ApiBuilder listener(Supplier<FutureListener> listener);

    ApiBuilder httpClient(HttpClient httpClient);

    ApiBuilder converterFactory(ConverterFactory converterFactory);

    <T> T build(Class<T> type);

    static ApiBuilder newBuilder() {
        return new DefaultApiBuilder();
    }
}
