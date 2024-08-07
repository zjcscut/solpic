package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.client.ahc4.ApacheHttpClientV4Impl;
import cn.vlts.solpic.core.http.client.jhc.JdkHttpClientImpl;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublishers;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscribers;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * ApacheHttpClientV5Impl test.
 *
 * @author throwable
 * @since 2024/8/6 星期二 14:19
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
                PayloadSubscribers.X.ofString());
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
}
