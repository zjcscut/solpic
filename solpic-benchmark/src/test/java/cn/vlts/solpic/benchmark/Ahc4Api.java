package cn.vlts.solpic.benchmark;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Apache HTTP client v4 API.
 *
 * @author throwable
 * @version v1
 * @since 2024/9/22 14:33
 */
public class Ahc4Api implements Api {

    private final String path;
    private final CloseableHttpClient client;

    public Ahc4Api(String path) {
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
