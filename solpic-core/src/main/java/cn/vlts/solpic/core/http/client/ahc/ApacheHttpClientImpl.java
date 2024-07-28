package cn.vlts.solpic.core.http.client.ahc;

import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.BaseHttpClient;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.io.IOException;

/**
 * HTTP client base on Apache HTTP client 5.x.
 *
 * @author throwable
 * @since 2024/7/24 00:29
 */
public class ApacheHttpClientImpl extends BaseHttpClient implements HttpClient {

    @Override
    protected <T> HttpResponse<T> sendInternal(HttpRequest request,
                                               PayloadPublisher payloadPublisher,
                                               PayloadSubscriber<T> payloadSubscriber) throws IOException {
        return null;
    }

    static {
//        ReflectionUtils.X.forName("");
    }
}
