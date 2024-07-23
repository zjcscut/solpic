package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.http.HttpOptional;
import cn.vlts.solpic.core.http.HttpVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HTTP option support.
 *
 * @author throwable
 * @since 2024/7/23 星期二 20:43
 */
public abstract class HttpOptionSupport implements HttpOptional {

    private final List<HttpVersion> httpVersions = new ArrayList<>();

    public void addHttpVersion(HttpVersion httpVersion) {
        this.httpVersions.add(httpVersion);
    }

    @Override
    public boolean supportHttpVersion(HttpVersion httpVersion) {
        return httpVersions.stream().anyMatch(hv -> hv.isSameAs(httpVersion));
    }

    @Override
    public List<HttpVersion> availableHttpVersions() {
        return Collections.unmodifiableList(this.httpVersions);
    }
}
