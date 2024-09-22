package cn.vlts.solpic.benchmark;

import okhttp3.*;

import java.util.concurrent.TimeUnit;

/**
 * OkHttp client API.
 *
 * @author throwable
 * @version v1
 * @since 2024/9/22 14:33
 */
public class OkHttpApi implements Api {

    private final String path;

    private final OkHttpClient client;

    public OkHttpApi(String path) {
        this.path = path;
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(25, 30, TimeUnit.MINUTES))
                .build();
    }

    @Override
    public String getString(String query) throws Exception {
        Request request = new Request.Builder()
                .url(path + "/getString? + query")
                .get()
                .build();
        return client.newCall(request).execute().body().string();
    }

    @Override
    public String postJson(String json) throws Exception {
        Request request = new Request.Builder()
                .url(path + "/postJson")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();
        return client.newCall(request).execute().body().string();
    }
}
