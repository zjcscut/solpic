package cn.vlts.solpic.core.http;

import java.net.URI;

/**
 * HTTP request.
 *
 * @author throwable
 * @since 2024/7/23 星期二 19:49
 */
public interface HttpRequest extends HttpMessage {

    String getScheme();

    void setScheme(String scheme);

    String getRawMethod();

    HttpMethod getMethod();

    void  setRawUri(String uri);

    String getRawUri();

    URI getUri();

    void setUri(URI uri);
}
