package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.common.HttpStatusCode;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.util.Attachable;
import cn.vlts.solpic.core.util.AttachmentKey;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Readonly HTTP response.
 *
 * @author throwable
 * @since 2024/7/28 01:33
 */
@RequiredArgsConstructor(staticName = "of")
public class ReadOnlyHttpResponse<T> implements HttpResponse<T> {

    private final HttpResponse<T> response;

    @Override
    public HttpStatusCode getStatusCode() {
        return this.response.getStatusCode();
    }

    @Override
    public HttpRequest getHttpRequest() {
        return ReadOnlyHttpRequest.of(this.response.getHttpRequest());
    }

    @Override
    public HttpClient getHttpClient() {
        return this.response.getHttpClient();
    }

    @Override
    public void setProtocolVersion(HttpVersion version) {

    }

    @Override
    public HttpVersion getProtocolVersion() {
        return this.response.getProtocolVersion();
    }

    @Override
    public HttpHeader getFirstHeader(String name) {
        return this.response.getFirstHeader(name);
    }

    @Override
    public String getFirstHeaderValue(String name) {
        return this.response.getFirstHeaderValue(name);
    }

    @Override
    public List<HttpHeader> getHeader(String name) {
        return this.response.getHeader(name);
    }

    @Override
    public List<String> getHeaderValue(String name) {
        return this.response.getHeaderValue(name);
    }

    @Override
    public Set<String> getAllHeaderNames() {
        return this.response.getAllHeaderNames();
    }

    @Override
    public List<HttpHeader> getAllHeaders() {
        return this.response.getAllHeaders();
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
        return this.response.containsHeader(name);
    }

    @Override
    public boolean containsHeader(HttpHeader header) {
        return this.response.containsHeader(header);
    }

    @Override
    public void consumeHeaders(Consumer<HttpHeader> consumer) {
        this.response.consumeHeaders(consumer);
    }

    @Override
    public void clearHeaders() {

    }

    @Override
    public void setContentLength(long contentLength) {

    }

    @Override
    public long getContentLength() {
        return this.response.getContentLength();
    }

    @Override
    public void setContentTypeValue(String contentType) {

    }

    @Override
    public String getContentTypeValue() {
        return this.response.getContentTypeValue();
    }

    @Override
    public void setContentType(ContentType contentType) {

    }

    @Override
    public ContentType getContentType() {
        return this.response.getContentType();
    }

    @Override
    public String getReasonPhrase() {
        return this.response.getReasonPhrase();
    }

    @Override
    public T getPayload() {
        return this.response.getPayload();
    }

    @Override
    public Map<AttachmentKey, Object> getAttachments() {
        return this.response.getAttachments();
    }

    @Override
    public Set<AttachmentKey> getAttachmentKeys() {
        return this.response.getAttachmentKeys();
    }

    @Override
    public <S> void addAttachment(AttachmentKey key, S value) {
        this.response.addAttachment(key, value);
    }

    @Override
    public <S> void setAttachment(AttachmentKey key, S value) {
        this.response.setAttachment(key, value);
    }

    @Override
    public <S> S getAttachment(AttachmentKey key) {
        return this.response.getAttachment(key);
    }

    @Override
    public <S> S getAttachment(AttachmentKey key, S defaultValue) {
        return this.response.getAttachment(key, defaultValue);
    }

    @Override
    public void copyAttachable(Attachable attachable) {

    }
}
