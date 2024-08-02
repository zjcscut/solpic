package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.codec.impl.JacksonCodec;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.client.jdk.JdkHttpClientImpl;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublishers;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscribers;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

/**
 * Test JdkHttpClientImpl.
 *
 * @author throwable
 * @since 2024/7/28 22:37
 */
public class TestJdkHttpClientImpl {

    private final JdkHttpClientImpl jdkHttpClientImpl = new JdkHttpClientImpl();

    @Test
    public void testSimpleSend() {
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), jdkHttpClientImpl);
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

    @Test
    public void testFlowSend() {
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), jdkHttpClientImpl);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        jdkHttpClientImpl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        HttpResponse<String> response = jdkHttpClientImpl.send(request, FlowPayloadPublishers.X.discarding(),
                FlowPayloadSubscribers.X.ofString());
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPayload());
        System.out.println(response.getContentLength());
        System.out.println(response.getPayload());
    }

    @Test
    public void testHttpBinSend() {
        Codec<?,?> codec = new JacksonCodec<>();
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), jdkHttpClientImpl);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        jdkHttpClientImpl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        HttpResponse<HttpBinResult> response = jdkHttpClientImpl.send(request, FlowPayloadPublishers.X.discarding(),
                codec.createFlowPayloadSubscriber(HttpBinResult.class));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPayload());
        System.out.println(response.getContentLength());
        System.out.println(response.getPayload());
    }

    @Data
    public static class HttpBinResult {

        private Map<String, String> args;
        private Map<String, String> headers;
        private String origin;
        private String url;
    }
}
