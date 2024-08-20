package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.RequestPayloadSupport;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;

/**
 * Api enhance support.
 *
 * @author throwable
 * @since 2024/8/21 00:30
 */
@SuppressWarnings("rawtypes")
public abstract class ApiEnhanceSupport {

    protected abstract boolean supportRequestPayloadConverter(ApiParameterMetadata metadata);

    protected abstract <S> Converter<S, RequestPayloadSupport> getRequestPayloadConverter(ApiParameterMetadata metadata);

    protected abstract boolean supportResponsePayloadSupplier(ApiParameterMetadata metadata);

    protected abstract <T> ResponsePayloadSupport<T> getResponsePayloadSupplier(ApiParameterMetadata metadata);

    protected abstract Codec getCodec();

    protected abstract HttpClient getHttpClient();
}
