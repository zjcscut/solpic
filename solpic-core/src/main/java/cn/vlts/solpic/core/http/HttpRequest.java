package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.common.HttpRequestStatus;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.util.Attachable;
import cn.vlts.solpic.core.util.AttachmentKey;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * HTTP request.
 *
 * @author throwable
 * @since 2024/7/23 星期二 19:49
 */
public interface HttpRequest extends HttpMessage, HttpOptional, Attachable {

    List<HttpMethod> METHODS_WITH_BODY = new ArrayList<>(Arrays.asList(HttpMethod.POST,
            HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.TRACE));

    String getScheme();

    void setScheme(String scheme);

    String getRawMethod();

    HttpMethod getMethod();

    void setRawUri(String uri);

    String getRawUri();

    URI getUri();

    void setUri(URI uri);

    default boolean supportPayload() {
        HttpMethod method = getMethod();
        return Objects.nonNull(method) && METHODS_WITH_BODY.contains(method);
    }

    <S extends RequestPayloadSupport> S getPayloadPublisher();

    HttpClient getHttpClient();

    HttpRequestStatus getStatus();

    static Builder newBuilder() {
        return new DefaultHttpRequest.Builder();
    }

    interface Builder {

        Builder minimumOption(HttpOption<?> httpOption);

        Builder availableOption(HttpOption<?> httpOption);

        <H> Builder option(HttpOption<H> httpOption, H value);

        <A> Builder attachment(AttachmentKey key, A value);

        Builder uri(URI uri);

        Builder method(HttpMethod method);

        Builder header(HttpHeader header);

        Builder header(String name, String value);

        Builder query(String name, String value);

        Builder path(String path);

        <S extends RequestPayloadSupport> Builder payloadPublisher(S payloadPublisher);

        HttpRequest build();
    }
}
