package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.HttpOptional;
import cn.vlts.solpic.core.http.HttpVersion;

import java.util.*;

/**
 * HTTP option support.
 *
 * @author throwable
 * @since 2024/7/23 星期二 20:43
 */
public abstract class HttpOptionSupport implements HttpOptional {

    protected final List<HttpVersion> httpVersions = new ArrayList<>();

    protected long availableOpts = -1;

    protected long minimumOpts = -1;

    protected long opts = -1;

    public void addHttpVersion(HttpVersion httpVersion) {
        this.httpVersions.add(httpVersion);
    }

    public void addHttpVersions(HttpVersion... httpVersions) {
        if (Objects.nonNull(httpVersions)) {
            this.httpVersions.addAll(Arrays.asList(httpVersions));
        }
    }

    @Override
    public boolean supportHttpVersion(HttpVersion httpVersion) {
        return httpVersions.stream().anyMatch(hv -> hv.isSameAs(httpVersion));
    }

    @Override
    public List<HttpVersion> availableHttpVersions() {
        return Collections.unmodifiableList(this.httpVersions);
    }

    @Override
    public boolean supportHttpOption(HttpOption<?> httpOption) {
        return this.opts > 0 && httpOption.support(this.opts);
    }

    @Override
    public List<HttpOption<?>> availableHttpOptions() {
        return Collections.emptyList();
    }

    @Override
    public List<HttpOption<?>> minimumHttpOptions() {
        return Collections.emptyList();
    }

    @Override
    public <T> T getHttpOptionValue(HttpOption<T> httpOption) {
        return null;
    }
}
