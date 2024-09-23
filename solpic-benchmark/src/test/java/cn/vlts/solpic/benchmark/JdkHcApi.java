package cn.vlts.solpic.benchmark;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * JDK HTTP client API.
 *
 * @author throwable
 * @version v1
 * @since 2024/9/22 14:33
 */
public class JdkHcApi implements Api {

    private final String path;

    private final HttpClient client;

    public JdkHcApi(String path) {
        this.path = path;
        client = HttpClient.newBuilder().build();
    }

    @Override
    public String getString(String query) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(path + "/getString?" + query)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
    }

    @Override
    public String postJson(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .uri(URI.create(path + "/postJson")).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
    }
}
