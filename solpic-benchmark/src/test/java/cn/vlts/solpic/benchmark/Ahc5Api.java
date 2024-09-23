package cn.vlts.solpic.benchmark;


import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
 * Apache HTTP client v5 API.
 *
 * @author throwable
 * @version v1
 * @since 2024/9/22 14:33
 */
public class Ahc5Api implements Api {

    private final String path;

    private final CloseableHttpClient client;

    public Ahc5Api(String path) {
        this.path = path;
        client = HttpClients.createDefault();
    }

    @Override
    public String getString(String query) throws Exception {
        HttpGet get = new HttpGet(this.path + "/getString?" + query);
        try (CloseableHttpResponse response = client.execute(get)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    @Override
    public String postJson(String json) throws Exception {
        HttpPost post = new HttpPost(this.path + "/postJson");
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(json));
        try (CloseableHttpResponse response = client.execute(post)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
}
