package cn.vlts.solpic.core.http.client.jdk;

import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpOptional;
import cn.vlts.solpic.core.http.HttpVersion;
import cn.vlts.solpic.core.http.client.BaseHttpClient;

/**
 * JDK HTTP client, base on HttpURLConnection.
 *
 * @author throwable
 * @since 2024/7/24 00:27
 */
public class JdkHttpClientImpl extends BaseHttpClient implements HttpClient, HttpOptional {

    public JdkHttpClientImpl() {
        init();
    }

    private void init() {
        // HttpURLConnection only support HTTP/1.0 and HTTP/1.1
        addHttpVersions(HttpVersion.HTTP_1, HttpVersion.HTTP_1_1);
    }
}
