package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.codec.impl.FastJson2Codec;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.PayloadSubscriber;
import cn.vlts.solpic.core.http.bind.MultipartData;
import cn.vlts.solpic.core.http.bind.UrlEncodedForm;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.spi.SpiLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HttpClient API test.
 *
 * @author throwable
 * @since 2024/8/26 星期一 10:13
 */
@SuppressWarnings("unchecked")
public class HttpClientApiTest {

    private static final int PORT = 18080;

    private static final String GET_STRING_RESULT = "Result of getString";

    private static ClientAndServer MOCK_SERVER;

    private static Codec CODEC;

    private static ApiResult API_RESULT;

    @BeforeClass
    public static void setup() {
        API_RESULT = new ApiResult();
        API_RESULT.setId(1L);
        API_RESULT.setName("Tome");
        API_RESULT.setCreateTime(LocalDateTime.now());
        CODEC = new FastJson2Codec<>();
        MOCK_SERVER = ClientAndServer.startClientAndServer(PORT);
        initMockClient(MOCK_SERVER);
    }

    private static void initMockClient(MockServerClient client) {
        client.when(HttpRequest.request().withMethod("GET").withPath("/api/getString"))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.TEXT_PLAIN).withBody(GET_STRING_RESULT));
        client.when(HttpRequest.request().withMethod("GET").withPath("/api/getObject"))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON).withBody(CODEC.toByteArray(API_RESULT)));
        ApiResult bodyResult = new ApiResult();
        bodyResult.setId(2L);
        bodyResult.setCreateTime(LocalDateTime.now());
        bodyResult.setName("Jerry");
        client.when(HttpRequest.request()
                        .withMethod("POST").withPath("/api/postJson"))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON).withBody(CODEC.toByteArray(bodyResult)));
        client.when(HttpRequest.request()
                        .withMethod("POST").withPath("/api/postForm").
                        withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .withBody(ParameterBody.params(
                                Parameter.param("foo", "bar"),
                                Parameter.param("1", "2")
                        ))
                )
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON).withBody(CODEC.toByteArray(bodyResult)));
        client.when(HttpRequest.request()
                        .withMethod("PUT").withPath("/api/putJson"))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON).withBody(CODEC.toByteArray(bodyResult)));
        client.when(HttpRequest.request()
                        .withMethod("PATCH").withPath("/api/patchJson"))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON).withBody(CODEC.toByteArray(bodyResult)));
        client.when(HttpRequest.request()
                        .withMethod("TRACE").withPath("/api/trace"))
                .respond(HttpResponse.response().withStatusCode(200));
        client.when(HttpRequest.request()
                        .withMethod("DELETE").withPath("/api/delete"))
                .respond(HttpResponse.response().withStatusCode(200));
        client.when(HttpRequest.request()
                        .withMethod("OPTIONS").withPath("/api/options"))
                .respond(HttpResponse.response().withStatusCode(200));
        client.when(HttpRequest.request()
                        .withMethod("HEAD").withPath("/api/head"))
                .respond(HttpResponse.response().withStatusCode(200));
    }

    @Test
    public void testAllHttpClientApis() {
        List<HttpClient> httpClients = SpiLoader.getSpiLoader(HttpClient.class).getAvailableServices();
        for (HttpClient hc : httpClients) {
            processGetString(hc);
            processGetObject(hc);
            processPostJson(hc);
            processPostForm(hc);
            processPutJson(hc);
            // HTTPUrlConnection不支持PATCH方法
            if (!hc.spec().contains("DefaultHttpClient")) {
                processPatchJson(hc);
            }
            processTrace(hc);
            processDelete(hc);
            processOptions(hc);
            processHead(hc);
        }
    }

    private void processGetString(HttpClient hc) {
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/getString"))
                .method(HttpMethod.GET)
                .build();
        cn.vlts.solpic.core.http.HttpResponse<String> response = hc.send(request, PayloadSubscribers.X.ofString());
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getPayload()).isNotNull();
        Assertions.assertThat(response.getPayload()).isEqualTo(GET_STRING_RESULT);
    }

    private void processGetObject(HttpClient hc) {
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/getObject"))
                .method(HttpMethod.GET)
                .build();
        PayloadSubscriber payloadSubscriber = CODEC.createPayloadSubscriber(ApiResult.class);
        cn.vlts.solpic.core.http.HttpResponse<ApiResult> response = hc.send(request, payloadSubscriber);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getPayload()).isNotNull();
        Assertions.assertThat(response.getPayload()).isEqualTo(API_RESULT);
    }

    private void processPostJson(HttpClient hc) {
        ApiResult apiResult = new ApiResult();
        apiResult.setId(2L);
        apiResult.setCreateTime(LocalDateTime.now());
        apiResult.setName("Jerry");
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/postJson"))
                .method(HttpMethod.POST)
                .payloadPublisher(CODEC.createPayloadPublisher(apiResult))
                .build();
        PayloadSubscriber payloadSubscriber = CODEC.createPayloadSubscriber(ApiResult.class);
        cn.vlts.solpic.core.http.HttpResponse<ApiResult> response = hc.send(request, payloadSubscriber);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getPayload()).isNotNull();
        Assertions.assertThat(response.getPayload()).isEqualTo(apiResult);
    }

    private void processPostForm(HttpClient hc) {
        ApiResult apiResult = new ApiResult();
        apiResult.setId(2L);
        apiResult.setCreateTime(LocalDateTime.now());
        apiResult.setName("Jerry");
        UrlEncodedForm form = UrlEncodedForm.newBuilder()
                .add("foo", "bar")
                .add("1", "2")
                .build();
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/postForm"))
                .method(HttpMethod.POST)
                .payloadPublisher(form)
                .build();
        PayloadSubscriber payloadSubscriber = CODEC.createPayloadSubscriber(ApiResult.class);
        cn.vlts.solpic.core.http.HttpResponse<ApiResult> response = hc.send(request, payloadSubscriber);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getPayload()).isNotNull();
        Assertions.assertThat(response.getPayload()).isEqualTo(apiResult);
    }

    private void processPostMultipart(HttpClient hc) {
        ApiResult apiResult = new ApiResult();
        apiResult.setId(2L);
        apiResult.setCreateTime(LocalDateTime.now());
        apiResult.setName("Jerry");
        MultipartData multipartData = MultipartData.newBuilder()
                .addTextPart("foo", "bar")
                .addBinaryPart("1", "2".getBytes())
                .build();
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/postMultipart"))
                .method(HttpMethod.POST)
                .payloadPublisher(multipartData)
                .build();
        PayloadSubscriber payloadSubscriber = CODEC.createPayloadSubscriber(ApiResult.class);
        cn.vlts.solpic.core.http.HttpResponse<ApiResult> response = hc.send(request, payloadSubscriber);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getPayload()).isNotNull();
        Assertions.assertThat(response.getPayload()).isEqualTo(apiResult);
    }

    private void processPutJson(HttpClient hc) {
        ApiResult apiResult = new ApiResult();
        apiResult.setId(2L);
        apiResult.setCreateTime(LocalDateTime.now());
        apiResult.setName("Jerry");
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/putJson"))
                .method(HttpMethod.PUT)
                .payloadPublisher(CODEC.createPayloadPublisher(apiResult))
                .build();
        PayloadSubscriber payloadSubscriber = CODEC.createPayloadSubscriber(ApiResult.class);
        cn.vlts.solpic.core.http.HttpResponse<ApiResult> response = hc.send(request, payloadSubscriber);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getPayload()).isNotNull();
        Assertions.assertThat(response.getPayload()).isEqualTo(apiResult);
    }

    private void processPatchJson(HttpClient hc) {
        ApiResult apiResult = new ApiResult();
        apiResult.setId(2L);
        apiResult.setCreateTime(LocalDateTime.now());
        apiResult.setName("Jerry");
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/patchJson"))
                .method(HttpMethod.PATCH)
                .payloadPublisher(CODEC.createPayloadPublisher(apiResult))
                .build();
        PayloadSubscriber payloadSubscriber = CODEC.createPayloadSubscriber(ApiResult.class);
        cn.vlts.solpic.core.http.HttpResponse<ApiResult> response = hc.send(request, payloadSubscriber);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getPayload()).isNotNull();
        Assertions.assertThat(response.getPayload()).isEqualTo(apiResult);
    }

    private void processTrace(HttpClient hc) {
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/trace"))
                .method(HttpMethod.TRACE)
                .payloadPublisher(PayloadPublishers.X.discarding())
                .build();
        cn.vlts.solpic.core.http.HttpResponse<?> response = hc.send(request, PayloadSubscribers.X.discarding());
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private void processDelete(HttpClient hc) {
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/delete"))
                .method(HttpMethod.DELETE)
                .payloadPublisher(PayloadPublishers.X.discarding())
                .build();
        cn.vlts.solpic.core.http.HttpResponse<?> response = hc.send(request, PayloadSubscribers.X.discarding());
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private void processOptions(HttpClient hc) {
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/options"))
                .method(HttpMethod.OPTIONS)
                .payloadPublisher(PayloadPublishers.X.discarding())
                .build();
        cn.vlts.solpic.core.http.HttpResponse<?> response = hc.send(request, PayloadSubscribers.X.discarding());
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    private void processHead(HttpClient hc) {
        cn.vlts.solpic.core.http.HttpRequest request = cn.vlts.solpic.core.http.HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:18080/api/head"))
                .method(HttpMethod.HEAD)
                .payloadPublisher(PayloadPublishers.X.discarding())
                .build();
        cn.vlts.solpic.core.http.HttpResponse<?> response = hc.send(request, PayloadSubscribers.X.discarding());
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode()).isNotNull();
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @AfterClass
    public static void stop() {
        MOCK_SERVER.stop();
    }

    @Data
    @EqualsAndHashCode(of = {"id", "name"})
    public static class ApiResult {

        private Long id;

        private String name;

        private LocalDateTime createTime;
    }
}
