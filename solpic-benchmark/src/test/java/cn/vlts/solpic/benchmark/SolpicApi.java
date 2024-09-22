package cn.vlts.solpic.benchmark;


import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;

import java.net.URI;

/**
 * JDK HTTP client API.
 *
 * @author throwable
 * @version v1
 * @since 2024/9/22 14:33
 */
public class SolpicApi implements Api {

    private final String path;

    private final HttpClient client;

    public SolpicApi(String path, HttpClient client) {
        this.path = path;
        this.client = client;
    }

    @Override
    public String getString(String query) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().method(HttpMethod.GET)
                .uri(URI.create(path + "/getString?" + query))
                .build();
        return client.sendSimple(request, PayloadSubscribers.X.ofString());
    }

    @Override
    public String postJson(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().method(HttpMethod.POST)
                .uri(URI.create(path + "/postJson"))
                .payloadPublisher(PayloadPublishers.X.ofString(json))
                .build();
        return client.sendSimple(request, PayloadSubscribers.X.ofString());
    }
}
