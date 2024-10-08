package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.codec.impl.JacksonCodec;
import cn.vlts.solpic.core.common.HttpClientType;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.flow.FlowPayloadPublishers;
import cn.vlts.solpic.core.http.flow.FlowPayloadSubscribers;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.spi.SpiLoader;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Test JdkHttpClientImpl.
 *
 * @author throwable
 * @since 2024/7/28 22:37
 */
public class TestOkHttpClientImpl {

    private final HttpClient okHttpClientImpl = SpiLoader.getSpiLoader(HttpClient.class)
            .getService(HttpClientType.OKHTTP.getCode());

    @Test
    public void testSimpleSend() {
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), okHttpClientImpl);
        okHttpClientImpl.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        okHttpClientImpl.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        okHttpClientImpl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        request.setPayloadPublisher(PayloadPublishers.X.discarding());
        HttpResponse<String> response = okHttpClientImpl.send(request, PayloadSubscribers.X.ofString());
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPayload());
        System.out.println(response.getContentLength());
        System.out.println(response.getPayload());
    }

    @Test
    public void testFlowSend() {
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), okHttpClientImpl);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        okHttpClientImpl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        request.setPayloadPublisher(FlowPayloadPublishers.X.discarding());
        HttpResponse<String> response = okHttpClientImpl.send(request, FlowPayloadSubscribers.X.ofString());
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPayload());
        System.out.println(response.getContentLength());
        System.out.println(response.getPayload());
    }

    @Test
    public void testHttpBinSend() {
        Codec<?, ?> codec = new JacksonCodec<>();
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), okHttpClientImpl);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_TRACING, true);
        okHttpClientImpl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        request.setPayloadPublisher(FlowPayloadPublishers.X.discarding());
        HttpResponse<HttpBinResult> response = okHttpClientImpl.send(request, codec.createFlowPayloadSubscriber(HttpBinResult.class));
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPayload());
        System.out.println(response.getContentLength());
        System.out.println(response.getPayload());
    }

    @Test
    public void testHttpBinScheduledSend() throws Exception {
        Codec<?, ?> codec = new JacksonCodec<>();
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), okHttpClientImpl);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_TRACING, true);
        okHttpClientImpl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        CompletableFuture<HttpResponse<HttpBinResult>> promise = new CompletableFuture<>();
        promise.whenComplete((response, throwable) -> {
            if (Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            } else {
                System.out.printf("result => %s\n", response.getPayload());
            }
        });
        request.setPayloadPublisher(FlowPayloadPublishers.X.discarding());
        ScheduledFuture<HttpResponse<HttpBinResult>> response = okHttpClientImpl.scheduledSend(request,
                codec.createFlowPayloadSubscriber(HttpBinResult.class), 3, TimeUnit.SECONDS, promise);
        Assert.assertNotNull(response);
        Thread.sleep(5000);
    }

    @Data
    public static class HttpBinResult {

        private Map<String, String> args;
        private Map<String, String> headers;
        private String origin;
        private String url;
    }
}
