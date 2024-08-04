package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.common.HttpRequestStatus;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.util.Attachable;
import cn.vlts.solpic.core.util.AttachmentKey;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Readonly HTTP request.
 *
 * @author throwable
 * @since 2024/7/28 01:29
 */
@RequiredArgsConstructor(staticName = "of")
public final class ReadOnlyHttpRequest implements HttpRequest {

    private final HttpRequest request;

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public String getRawMethod() {
        return request.getRawMethod();
    }

    @Override
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public void setRawUri(String uri) {

    }

    @Override
    public String getRawUri() {
        return request.getRawUri();
    }

    @Override
    public URI getUri() {
        return request.getUri();
    }

    @Override
    public void setUri(URI uri) {

    }

    @Override
    public HttpClient getHttpClient() {
        return request.getHttpClient();
    }

    @Override
    public void setProtocolVersion(HttpVersion version) {

    }

    @Override
    public HttpVersion getProtocolVersion() {
        return request.getProtocolVersion();
    }

    @Override
    public HttpHeader getFirstHeader(String name) {
        return request.getFirstHeader(name);
    }

    @Override
    public String getFirstHeaderValue(String name) {
        return request.getFirstHeaderValue(name);
    }

    @Override
    public List<HttpHeader> getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public List<String> getHeaderValue(String name) {
        return request.getHeaderValue(name);
    }

    @Override
    public Set<String> getAllHeaderNames() {
        return request.getAllHeaderNames();
    }

    @Override
    public List<HttpHeader> getAllHeaders() {
        return request.getAllHeaders();
    }

    @Override
    public void addHeader(HttpHeader header) {

    }

    @Override
    public void addHeaders(HttpHeader... headers) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setHeader(HttpHeader header) {

    }

    @Override
    public void setHeaders(HttpHeader... headers) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void removeHeader(HttpHeader header) {

    }

    @Override
    public void removeHeaders(String name) {

    }

    @Override
    public void removeHeaders(Predicate<HttpHeader> predicate) {

    }

    @Override
    public boolean containsHeader(String name) {
        return request.containsHeader(name);
    }

    @Override
    public boolean containsHeader(HttpHeader header) {
        return request.containsHeader(header);
    }

    @Override
    public void consumeHeaders(Consumer<HttpHeader> consumer) {

    }

    @Override
    public void clearHeaders() {

    }

    @Override
    public void setContentLength(long contentLength) {

    }

    @Override
    public long getContentLength() {
        return request.getContentLength();
    }

    @Override
    public void setContentTypeValue(String contentType) {

    }

    @Override
    public String getContentTypeValue() {
        return request.getContentTypeValue();
    }

    @Override
    public void setContentType(ContentType contentType) {

    }

    @Override
    public ContentType getContentType() {
        return request.getContentType();
    }

    @Override
    public List<HttpVersion> availableHttpVersions() {
        return request.availableHttpVersions();
    }

    @Override
    public boolean supportHttpVersion(HttpVersion httpVersion) {
        return request.supportHttpVersion(httpVersion);
    }

    @Override
    public List<HttpOption<?>> getAvailableHttpOptions() {
        return request.getAvailableHttpOptions();
    }

    @Override
    public List<HttpOption<?>> getMinimumHttpOptions() {
        return request.getMinimumHttpOptions();
    }

    @Override
    public List<HttpOption<?>> getHttpOptions() {
        return request.getHttpOptions();
    }

    @Override
    public boolean supportHttpOption(HttpOption<?> httpOption) {
        return request.supportHttpOption(httpOption);
    }

    @Override
    public <T> T getHttpOptionValue(HttpOption<T> httpOption) {
        return request.getHttpOptionValue(httpOption);
    }

    @Override
    public Map<AttachmentKey, Object> getAttachments() {
        return request.getAttachments();
    }

    @Override
    public Set<AttachmentKey> getAttachmentKeys() {
        return request.getAttachmentKeys();
    }

    @Override
    public <T> void addAttachment(AttachmentKey key, T value) {
        request.addAttachment(key, value);
    }

    @Override
    public <T> void setAttachment(AttachmentKey key, T value) {
        request.setAttachment(key, value);
    }

    @Override
    public <T> T getAttachment(AttachmentKey key) {
        return request.getAttachment(key);
    }

    @Override
    public <T> T getAttachment(AttachmentKey key, T defaultValue) {
        return request.getAttachment(key, defaultValue);
    }

    @Override
    public void copyAttachable(Attachable attachable) {

    }

    @Override
    public HttpRequestStatus getStatus() {
        return request.getStatus();
    }

    @Override
    public <T> void addHttpOption(HttpOption<T> httpOption, T configValue) {

    }

    @Override
    public <T> void setHttpOption(HttpOption<T> httpOption, T configValue) {

    }
}
