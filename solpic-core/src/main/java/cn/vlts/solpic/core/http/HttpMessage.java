package cn.vlts.solpic.core.http;

import java.util.List;
import java.util.function.Predicate;

/**
 * HTTP message.
 *
 * @author throwable
 * @since 2024/7/19 星期五 17:34
 */
public interface HttpMessage {

    void setProtocolVersion(HttpVersion version);

    HttpVersion protocolVersion();

    HttpHeader getFirstHeader(String name);

    String getFirstHeaderValue(String name);

    List<HttpHeader> getHeader(String name);

    List<String> getHeaderValue(String name);

    List<HttpHeader> getAllHeaders();

    void addHeader(HttpHeader header);

    void addHeaders(HttpHeader... headers);

    void addHeader(String name, String value);

    void setHeader(HttpHeader header);

    void setHeaders(HttpHeader... headers);

    void setHeader(String name, String value);

    void removeHeader(HttpHeader header);

    void removeHeaders(String name);

    void removeHeaders(Predicate<HttpHeader> predicate);

    boolean containsHeader(String name);

    boolean containsHeader(HttpHeader header);

    void clearHeaders();
}
