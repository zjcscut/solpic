package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.config.HttpOption;

import java.util.List;

/**
 * HTTP client.
 *
 * @author throwable
 * @since 2024/7/23 星期二 17:59
 */
public interface HttpClient {

    List<HttpVersion> availableHttpVersions();

    boolean supportHttpVersion(HttpVersion httpVersion);

    List<HttpOption<?>> availableHttpOptions();

    List<HttpOption<?>> minimumHttpOptions();

    boolean supportHttpOption(HttpOption<?> httpOption);
}
