package cn.vlts.solpic.core.http.client.jhc;

import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.io.IOException;

/**
 * JDK11 HTTP client, base on java.net.http.HttpClient.
 *
 * @author throwable
 * @since 2024/8/6 星期二 16:37
 */
public class JdkHttpClientImpl extends BaseHttpClient implements HttpClient, HttpOptional {

    public JdkHttpClientImpl() {
        super();
        init();
    }

    private void init() {

    }

    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               RequestPayloadSupport payloadPublisher,
                                               ResponsePayloadSupport<?> payloadSubscriber) throws IOException {
        return null;
    }

    static {
        ReflectionUtils.X.forName("java.net.http.HttpClient");
    }
}
