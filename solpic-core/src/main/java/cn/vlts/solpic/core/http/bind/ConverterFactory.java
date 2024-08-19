package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.RequestPayloadSupport;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * Converter factory.
 *
 * @author throwable
 * @since 2024/8/17 21:00
 */
public abstract class ConverterFactory<S, T> {

    public boolean supportRequestConverter(ApiParameterMetadata metadata) {
        return false;
    }

    public Converter<S, RequestPayloadSupport> newRequestConverter(ApiParameterMetadata metadata) {
        return null;
    }

    public boolean supportResponseSupplier(ApiParameterMetadata metadata) {
        return false;
    }

    public Supplier<ResponsePayloadSupport<T>> newResponseSupplier(ApiParameterMetadata metadata) {
        return null;
    }
}
