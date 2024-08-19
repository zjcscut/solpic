package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.codec.impl.JacksonCodec;
import cn.vlts.solpic.core.http.HttpBinResult;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.util.function.Supplier;

/**
 * Httpbin converter factory.
 *
 * @author throwable
 * @since 2024/8/19 星期一 9:40
 */
public class HttpBinConverterFactory extends ConverterFactory {

    final Codec codec = new JacksonCodec();

    @Override
    public boolean supportResponseSupplier(ApiParameterMetadata metadata) {
        return true;
    }

    @Override
    public Supplier<ResponsePayloadSupport> newResponseSupplier(ApiParameterMetadata metadata) {
        ReflectionUtils.ParameterizedTypeInfo pti = ReflectionUtils.X.getParameterizedTypeInfo(metadata.getReturnType());
        Class<?> rawType = pti.getRawClass(pti.getMaxDepth(), 0);
        if (HttpBinResult.class.isAssignableFrom(rawType)) {
            return () -> codec.createPayloadSubscriber(rawType);
        }
        return super.newResponseSupplier(metadata);
    }
}
