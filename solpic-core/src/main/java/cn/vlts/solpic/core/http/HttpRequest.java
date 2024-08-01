package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.common.HttpRequestStatus;
import cn.vlts.solpic.core.util.Attachable;

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
            HttpMethod.PUT, HttpMethod.PATCH));

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

    HttpClient getHttpClient();

    HttpRequestStatus getStatus();
}
