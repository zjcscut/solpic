package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.client.jdk.DefaultJdkHttpClientImpl;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * Test JdkHttpClientImpl.
 *
 * @author throwable
 * @since 2024/7/28 22:37
 */
public class TestDefaultJdkHttpClientImpl {

    private final DefaultJdkHttpClientImpl jdkHttpClientImpl = new DefaultJdkHttpClientImpl();

    @Test
    public void testSimpleSend() {
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"));
        request.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        jdkHttpClientImpl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        HttpResponse<String> response = jdkHttpClientImpl.send(request, PayloadPublishers.DEFAULT.discarding(),
                PayloadSubscribers.DEFAULT.ofString());
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPayload());
        System.out.println(response.getContentLength());
        System.out.println(response.getPayload());
    }
}
