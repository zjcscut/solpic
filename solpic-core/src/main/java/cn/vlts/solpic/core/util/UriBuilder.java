package cn.vlts.solpic.core.util;

import cn.vlts.solpic.core.common.UriScheme;
import lombok.Getter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * URI builder.
 *
 * @author throwable
 * @since 2024/7/23 星期二 19:55
 */
@Getter
public final class UriBuilder {

    private static final String QUERY_PARAM_SEPARATOR = "&";

    private static final String PARAM_VALUE_SEPARATOR = "=";

    private static final String PATH_SEPARATOR = "/";

    private static final String CON = ":";

    private static final String Q = "?";

    private static final String FRAGMENT_SEPARATOR = "#";

    private static final String AT = "@";

    private final Charset charset;

    private String schema;

    private String host;

    private int port = -1;

    private String encodedPath;

    private List<String> pathSegments;

    private String encodedQuery;

    private List<Pair> queryParams;

    private String encodedFragment;

    private String fragment;

    private transient String encodedSchemeSpecificPart;

    private transient String schemeSpecificPart;

    private String encodedUserInfo;

    private String userInfo;

    private String encodedAuthority;

    private String authority;

    private boolean removeTrailingSlash;

    private volatile boolean dirty;

    public static UriBuilder newInstance(String uri) {
        return newInstance(URI.create(uri), StandardCharsets.UTF_8);
    }

    public static UriBuilder newInstance(String uri, Charset charset) {
        return newInstance(URI.create(uri), charset);
    }

    public static UriBuilder newInstance(URI uri) {
        return new UriBuilder(uri, StandardCharsets.UTF_8);
    }

    public static UriBuilder newInstance(URI uri, Charset charset) {
        return new UriBuilder(uri, charset);
    }

    private UriBuilder(URI uri, Charset charset) {
        this.charset = charset;
        parseUri(uri);
    }

    public UriBuilder forceRemoveTrailingSlash() {
        this.removeTrailingSlash = true;
        return this;
    }

    public UriBuilder withScheme(String scheme) {
        this.schema = scheme;
        this.dirty = true;
        return this;
    }

    public UriBuilder withHost(String host) {
        this.host = host;
        this.dirty = true;
        resetSchemeSpecificPart();
        return this;
    }

    public UriBuilder withPort(int port) {
        this.port = port;
        this.dirty = true;
        resetSchemeSpecificPart();
        return this;
    }

    public UriBuilder withPath(String path) {
        setPathString(path);
        this.dirty = true;
        return this;
    }

    public UriBuilder appendPath(String path) {
        appendPathString(path);
        return this;
    }

    public UriBuilder appendPaths(String... paths) {
        for (String path : paths) {
            appendPathString(path);
        }
        return this;
    }

    public UriBuilder addQueryParameter(String name, String value) {
        addQueryPair(name, value);
        return this;
    }

    public UriBuilder addQueryParameters(Pair... queryParameters) {
        for (Pair pair : queryParameters) {
            addQueryPair(pair.name(), pair.value());
        }
        return this;
    }

    public UriBuilder withFragment(String fragment) {
        setFragmentString(fragment);
        return this;
    }

    public UriBuilder withUserInfo(String userInfo) {
        setUserinfoString(userInfo);
        return this;
    }

    public UriBuilder withAuthority(String authority) {
        setAuthorityString(authority);
        return this;
    }

    public URI build() {
        URI uri = URI.create(buildString());
        if (dirty) {
            encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
            schemeSpecificPart = uri.getSchemeSpecificPart();
        }
        return uri;
    }

    public static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void parseUri(URI uri) {
        schema = uri.getScheme();
        host = uri.getHost();
        port = uri.getPort();
        if (port < 0) {
            if (UriScheme.HTTPS.isSameAs(schema)) {
                port = 443;
            } else {
                port = 80;
            }
        }
        encodedPath = uri.getRawPath();
        pathSegments = new ArrayList<>();
        if (Objects.nonNull(uri.getPath())) {
            pathSegments.addAll(Arrays.asList(uri.getPath().split(PATH_SEPARATOR)));
        }
        encodedQuery = uri.getRawQuery();
        queryParams = new ArrayList<>();
        if (Objects.nonNull(uri.getQuery())) {
            String[] queryArray = uri.getQuery().split(QUERY_PARAM_SEPARATOR);
            for (String query : queryArray) {
                String[] queryParam = query.split(PARAM_VALUE_SEPARATOR);
                if (2 == queryParam.length) {
                    queryParams.add(Pair.of(queryParam[0], queryParam[1]));
                }
            }
        }
        fragment = uri.getRawFragment();
        encodedFragment = uri.getFragment();
        encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
        schemeSpecificPart = uri.getSchemeSpecificPart();
        encodedUserInfo = uri.getRawUserInfo();
        userInfo = uri.getUserInfo();
        encodedAuthority = uri.getRawAuthority();
        authority = uri.getAuthority();
    }

    public UriBuilder reset() {
        host = null;
        port = -1;
        encodedPath = null;
        pathSegments = new ArrayList<>();
        encodedQuery = null;
        queryParams = new ArrayList<>();
        encodedFragment = null;
        fragment = null;
        encodedSchemeSpecificPart = null;
        schemeSpecificPart = null;
        encodedUserInfo = null;
        userInfo = null;
        encodedAuthority = null;
        authority = null;
        removeTrailingSlash = false;
        return this;
    }

    private void resetSchemeSpecificPart() {
        encodedSchemeSpecificPart = null;
        schemeSpecificPart = null;
    }

    private void setPathString(String pathString) {
        this.dirty = true;
        resetSchemeSpecificPart();
        String encodePaths = URLEncoder.encode(pathString);
        if (removeTrailingSlash) {
            boolean matched = encodePaths.endsWith(PATH_SEPARATOR);
            if (matched) {
                encodePaths = encodePaths.substring(0, encodePaths.lastIndexOf(PATH_SEPARATOR));
            }
        }
        this.pathSegments = new ArrayList<>();
        this.encodedPath = encodePaths;
        String[] paths = pathString.split(PATH_SEPARATOR);
        pathSegments.addAll(Arrays.asList(paths));
    }

    private void appendPathString(String pathString) {
        this.dirty = true;
        resetSchemeSpecificPart();
        String encodePath = encodeValue(pathString);
        if (removeTrailingSlash) {
            boolean matched = encodePath.endsWith(PATH_SEPARATOR);
            if (matched) {
                encodePath = encodePath.substring(0, encodePath.lastIndexOf(PATH_SEPARATOR));
            }
        }
        this.encodedPath = Objects.isNull(this.encodedPath) ? encodePath :
                (encodePath.startsWith(PATH_SEPARATOR) ? this.encodedPath + encodePath
                        : this.encodedPath + PATH_SEPARATOR + encodePath);
        this.pathSegments.add(pathString);
    }

    private void addQueryPair(String name, String value) {
        this.dirty = true;
        resetSchemeSpecificPart();
        String encodeQuery = encodeValue(name) + PARAM_VALUE_SEPARATOR + encodeValue(value);
        this.encodedQuery = Objects.isNull(this.encodedQuery) ? encodeQuery :
                this.encodedQuery + QUERY_PARAM_SEPARATOR + encodeQuery;
        this.queryParams.add(Pair.of(name, value));
    }

    private void setFragmentString(String fragmentString) {
        this.dirty = true;
        resetSchemeSpecificPart();
        this.encodedFragment = encodeValue(fragmentString);
        this.fragment = fragmentString;
    }

    private void setUserinfoString(String userinfoString) {
        this.dirty = true;
        resetSchemeSpecificPart();
        this.encodedUserInfo = encodeValue(userinfoString);
        this.userInfo = userinfoString;
    }

    private void setAuthorityString(String authorityString) {
        this.dirty = true;
        resetSchemeSpecificPart();
        this.encodedAuthority = encodeValue(authorityString);
        this.authority = authorityString;
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, charset.name());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String buildString() {
        StringBuilder builder = new StringBuilder();
        // scheme
        if (Objects.nonNull(schema)) {
            builder.append(schema).append(CON);
        }
        if (Objects.nonNull(encodedSchemeSpecificPart)) {
            builder.append(encodedSchemeSpecificPart);
        } else {
            // authority
            boolean useAuthority = false;
            if (Objects.nonNull(encodedAuthority)) {
                builder.append(PATH_SEPARATOR).append(PATH_SEPARATOR).append(encodedAuthority);
                useAuthority = true;
            } else if (Objects.nonNull(host)) {
                builder.append(PATH_SEPARATOR).append(PATH_SEPARATOR);
                // user info
                if (Objects.nonNull(encodedUserInfo)) {
                    builder.append(encodedUserInfo).append(AT);
                }
                // host
                String hostString = host;
                if (removeTrailingSlash) {
                    int i = hostString.lastIndexOf(PATH_SEPARATOR);
                    if (i != -1) {
                        hostString = hostString.substring(0, i);
                    }
                }
                builder.append(hostString);
                useAuthority = true;
            }
            // port
            if (port >= 0) {
                if (!Objects.equals(schema, UriScheme.HTTP.getValue()) &&
                        !Objects.equals(schema, UriScheme.HTTPS.getValue())) {
                    builder.append(CON).append(port);
                }
            }
            // path
            if (!pathSegments.isEmpty()) {
                StringJoiner pathJoiner = new StringJoiner(PATH_SEPARATOR);
                for (String pathSegment : pathSegments) {
                    if (pathSegment.startsWith(PATH_SEPARATOR)) {
                        pathSegment = pathSegment.substring(1);
                    }
                    pathJoiner.add(encodeValue(pathSegment));
                }
                String pathToUse = pathJoiner.toString();
                if (useAuthority && !pathToUse.startsWith(PATH_SEPARATOR)) {
                    builder.append(PATH_SEPARATOR);
                }
                if (removeTrailingSlash) {
                    boolean matched = pathToUse.endsWith(PATH_SEPARATOR);
                    if (matched) {
                        pathToUse = pathToUse.substring(0, pathToUse.lastIndexOf(PATH_SEPARATOR));
                    }
                }
                builder.append(pathToUse);
            }
            // query
            if (!queryParams.isEmpty()) {
                builder.append(Q);
                StringJoiner queryJoiner = new StringJoiner(QUERY_PARAM_SEPARATOR);
                for (Pair pair : queryParams) {
                    queryJoiner.add(encodeValue(pair.name()) + PARAM_VALUE_SEPARATOR + encodeValue(pair.value()));
                }
                builder.append(queryJoiner);
            }
        }
        // fragment
        if (Objects.nonNull(encodedFragment)) {
            builder.append(FRAGMENT_SEPARATOR).append(encodedFragment);
        }
        return builder.toString();
    }
}
