package cn.vlts.solpic.core.http;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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

    Set<String> getAllHeaderNames();

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

    void consumeHeaders(Consumer<HttpHeader> consumer);

    void clearHeaders();

    void setContentLength(long contentLength);

    long getContentLength();

    void setContentTypeValue(String contentType);

    String getContentTypeValue();

    void setContentType(ContentType contentType);

    ContentType getContentType();
}
