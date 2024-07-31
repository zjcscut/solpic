package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.config.HttpOptionParser;
import cn.vlts.solpic.core.config.HttpOptions;
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

    protected final Map<HttpOption<?>, Object> options = new HashMap<>();

    protected long availableOpts = -1;

    protected long minimumOpts = -1;

    @Override
    public boolean supportHttpVersion(HttpVersion httpVersion) {
        return httpVersions.stream().anyMatch(hv -> hv.isSameAs(httpVersion));
    }

    @Override
    public List<HttpVersion> availableHttpVersions() {
        return Collections.unmodifiableList(httpVersions);
    }

    @Override
    public boolean supportHttpOption(HttpOption<?> httpOption) {
        return this.options.containsKey(httpOption);
    }

    @Override
    public List<HttpOption<?>> getAvailableHttpOptions() {
        return this.availableOpts == -1 ? Collections.emptyList() : HttpOptions.getMatchedOptions(this.availableOpts);
    }

    @Override
    public List<HttpOption<?>> getMinimumHttpOptions() {
        return this.minimumOpts == -1 ? Collections.emptyList() : HttpOptions.getMatchedOptions(this.minimumOpts);
    }

    @Override
    public List<HttpOption<?>> getHttpOptions() {
        return Collections.unmodifiableList(new ArrayList<>(this.options.keySet()));
    }

    @Override
    public <T> T getHttpOptionValue(HttpOption<T> httpOption) {
        Class<T> type = httpOption.valueType();
        T configValue = type.cast(options.get(httpOption));
        if (Objects.nonNull(configValue)) {
            return HttpOptionParser.X.parseOptionValue(httpOption, configValue);
        }
        return null;
    }

    public void addHttpVersion(HttpVersion httpVersion) {
        this.httpVersions.add(httpVersion);
    }

    public void addHttpVersions(HttpVersion... httpVersions) {
        if (Objects.nonNull(httpVersions)) {
            this.httpVersions.addAll(Arrays.asList(httpVersions));
        }
    }

    public void addAvailableHttpOption(HttpOption<?> httpOption) {
        this.availableOpts |= httpOption.id();
    }

    public void addMinimumHttpOption(HttpOption<?> httpOption) {
        this.minimumOpts |= httpOption.id();
    }

    public <T> void addHttpOption(HttpOption<T> httpOption, T configValue) {
        this.options.putIfAbsent(httpOption, configValue);
    }

    public <T> void setHttpOption(HttpOption<T> httpOption, T configValue) {
        this.options.put(httpOption, configValue);
    }
}