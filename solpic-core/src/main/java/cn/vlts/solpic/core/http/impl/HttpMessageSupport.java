package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.common.HttpHeaders;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpHeader;
import cn.vlts.solpic.core.http.HttpMessage;
import cn.vlts.solpic.core.http.HttpVersion;
import cn.vlts.solpic.core.util.CaseInsensitiveMap;
import cn.vlts.solpic.core.util.Cis;
import cn.vlts.solpic.core.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * HTTP message support.
 *
 * @author throwable
 * @since 2024/7/19 星期五 17:35
 */
public abstract class HttpMessageSupport implements HttpMessage {

    private final Map<Cis, List<String>> headers = new CaseInsensitiveMap<>();

    private HttpVersion httpVersion = HttpVersion.defaultVersion();

    @Override
    public HttpVersion protocolVersion() {
        return this.httpVersion;
    }

    @Override
    public void setProtocolVersion(HttpVersion version) {
        this.httpVersion = version;
    }

    @Override
    public HttpHeader getFirstHeader(String name) {
        return Optional.ofNullable(this.headers.get(Cis.of(name)))
                .map(valueList -> BasicHttpHeader.of(name, valueList.get(0))).orElse(null);
    }

    @Override
    public String getFirstHeaderValue(String name) {
        return Optional.ofNullable(this.headers.get(Cis.of(name))).map(valueList -> valueList.get(0)).orElse(null);
    }

    @Override
    public List<HttpHeader> getHeader(String name) {
        return Optional.ofNullable(this.headers.get(Cis.of(name)))
                .map(valueList -> valueList.stream().map(v -> (HttpHeader) BasicHttpHeader.of(name, v))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    @Override
    public List<String> getHeaderValue(String name) {
        return Collections.unmodifiableList(this.headers.get(Cis.of(name)));
    }

    @Override
    public Set<String> getAllHeaderNames() {
        Set<String> headerNames = new HashSet<>();
        this.headers.forEach((k, valuelist) -> headerNames.add(k.toString()));
        return headerNames;
    }

    @Override
    public List<HttpHeader> getAllHeaders() {
        List<HttpHeader> headers = new ArrayList<>();
        this.headers.forEach((k, valuelist) -> valuelist.forEach(v -> headers.add(BasicHttpHeader.of(k.toString(), v))));
        return headers;
    }

    @Override
    public void addHeader(HttpHeader header) {
        addHeader0(header.name(), header.value());
    }

    @Override
    public void addHeaders(HttpHeader... headers) {
        if (Objects.nonNull(headers)) {
            for (HttpHeader header : headers) {
                addHeader(header);
            }
        }
    }

    @Override
    public void addHeader(String name, String value) {
        addHeader0(name, value);
    }

    @Override
    public void setHeader(HttpHeader header) {
        setHeader0(header.name(), header.value());
    }

    @Override
    public void setHeaders(HttpHeader... headers) {
        if (Objects.nonNull(headers)) {
            setHeaders0(headers);
        }
    }

    @Override
    public void setHeader(String name, String value) {
        setHeader0(name, value);
    }

    @Override
    public void removeHeader(HttpHeader header) {
        removeHeaders(httpHeader -> Objects.equals(httpHeader.name(), header.name()) &&
                Objects.equals(httpHeader.value(), header.value()));
    }

    @Override
    public void removeHeaders(String name) {
        removeHeaders(httpHeader -> Objects.equals(httpHeader.name(), name));
    }

    @Override
    public void removeHeaders(Predicate<HttpHeader> predicate) {
        removeHeaders0(predicate);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.headers.containsKey(Cis.of(name));
    }

    @Override
    public boolean containsHeader(HttpHeader header) {
        AtomicBoolean found = new AtomicBoolean();
        Cis name = Cis.of(header.name());
        this.headers.forEach((k, valuelist) -> {
            if (Objects.equals(k, name)) {
                if (valuelist.contains(header.value())) {
                    found.set(true);
                }
            }
        });
        return found.get();
    }

    @Override
    public void consumeHeaders(Consumer<HttpHeader> consumer) {
        this.headers.forEach((k, valuelist) -> valuelist.forEach(v -> consumer.accept(BasicHttpHeader.of(k.toString(), v))));
    }

    @Override
    public void clearHeaders() {
        this.headers.clear();
    }

    @Override
    public void setContentLength(long contentLength) {
        setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
    }

    @Override
    public long getContentLength() {
        return Optional.ofNullable(getFirstHeaderValue(HttpHeaders.CONTENT_LENGTH)).map(Long::parseLong)
                .orElse(0L);
    }

    @Override
    public void setContentTypeValue(String contentType) {
        setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }

    @Override
    public String getContentTypeValue() {
        return getFirstHeaderValue(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    public void setContentType(ContentType contentType) {
        setHeader(HttpHeaders.CONTENT_TYPE, contentType.getValue());
    }

    @Override
    public ContentType getContentType() {
        String contentTypeValue = getFirstHeaderValue(HttpHeaders.CONTENT_TYPE);
        if (Objects.nonNull(contentTypeValue)) {
            return ContentType.parse(contentTypeValue);
        }
        return null;
    }

    public void addHeader0(String name, String value) {
        this.headers.compute(Cis.of(name), (k, valuelist) -> {
            List<String> newValueList = new ArrayList<>(Objects.isNull(valuelist) ? new ArrayList<>() : valuelist);
            newValueList.add(value);
            return newValueList;
        });
    }

    public void setHeader0(String name, String value) {
        this.headers.put(Cis.of(name), Collections.singletonList(value));
    }

    public void setHeaders0(HttpHeader... headers) {
        Stream.of(headers).collect(Collectors.groupingBy(Pair::name)).forEach((k, v) ->
                this.headers.put(Cis.of(k), v.stream().map(Pair::value).collect(Collectors.toList())));
    }

    public void removeHeaders0(Predicate<HttpHeader> predicate) {
        final List<Cis> keysToRemove = new ArrayList<>();
        this.headers.forEach((k, valuelist) -> {
            valuelist.removeIf(value -> predicate.test(BasicHttpHeader.of(k.toString(), value)));
            if (valuelist.isEmpty()) {
                keysToRemove.add(k);
            }
        });
        if (!keysToRemove.isEmpty()) {
            keysToRemove.forEach(this.headers::remove);
        }
    }
}
