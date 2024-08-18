package cn.vlts.solpic.core.http.client;

import cn.vlts.solpic.core.common.HttpClientType;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.client.ahc4.ApacheHttpClientV4Impl;
import cn.vlts.solpic.core.http.client.ahc5.ApacheHttpClientV5Impl;
import cn.vlts.solpic.core.http.impl.DefaultHttpRequest;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.spi.SpiLoader;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * ApacheHttpClientV5Impl test.
 *
 * @author throwable
 * @since 2024/8/6 星期二 14:19
 */
public class TestApacheHttpClientV4Impl {

    private final HttpClient apacheHttpClientV4Impl = SpiLoader.getSpiLoader(HttpClient.class)
            .getService(HttpClientType.APACHE_HTTPCLIENT_V4.getCode());

    @Test
    public void testSimpleSend() {
        DefaultHttpRequest request = new DefaultHttpRequest(HttpMethod.GET, URI.create("https://httpbin.org/get"), apacheHttpClientV4Impl);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_LOGGING, true);
        request.addHttpOption(HttpOptions.HTTP_ENABLE_EXECUTE_PROFILE, true);
        request.setPayloadPublisher(PayloadPublishers.X.discarding());
        apacheHttpClientV4Impl.addHttpOption(HttpOptions.HTTP_RESPONSE_COPY_ATTACHMENTS, true);
        HttpResponse<String> response = apacheHttpClientV4Impl.send(request, PayloadSubscribers.X.ofString());
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getPayload());
        System.out.println(response.getContentLength());
        System.out.println(response.getPayload());
    }
}
