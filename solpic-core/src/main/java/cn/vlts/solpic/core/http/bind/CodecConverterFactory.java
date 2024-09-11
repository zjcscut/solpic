package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.RequestPayloadSupport;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;

import java.util.function.Supplier;

/**
 * ConverterFactory impl base on Codec.
 *
 * @author throwable
 * @version v1
 * @since 2024/9/11 星期三 17:49
 */
public class CodecConverterFactory<S, T> extends ConverterFactory<S, T> {

    private final Codec<S, T> codec;

    private CodecConverterFactory(Codec<S, T> codec) {
        this.codec = codec;
    }

    public static <S, T> ConverterFactory<S, T> newInstance(Codec<S, T> codec) {
        return new CodecConverterFactory<>(codec);
    }

    @Override
    public boolean supportRequestConverter(ApiParameterMetadata metadata) {
        return true;
    }

    @Override
    public Converter<S, RequestPayloadSupport> newRequestConverter(ApiParameterMetadata metadata) {
        return codec::createPayloadPublisher;
    }

    @Override
    public boolean supportResponseSupplier(ApiParameterMetadata metadata) {
        return true;
    }

    @Override
    public Supplier<ResponsePayloadSupport<T>> newResponseSupplier(ApiParameterMetadata metadata) {
        return () -> codec.createPayloadSubscriber(metadata.getReturnType());
    }
}
