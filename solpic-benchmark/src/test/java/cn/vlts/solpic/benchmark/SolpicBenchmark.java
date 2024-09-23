package cn.vlts.solpic.benchmark;

import cn.vlts.solpic.core.Solpic;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Solpic benchmark.
 *
 * @author throwable
 * @since 2024/9/22 14:35
 */
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 6, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.AverageTime)
public class SolpicBenchmark {

    private static final int PORT = 18080;
    private static final String PATH = "/api";
    private static final String QUERY = "foo=bar";
    private static final String JSON = "{\"foo\":\"bar\"}";

    private Api jdkHcApi;
    private Api okHttpApi;
    private Api ahc4Api;
    private Api ahc5Api;
    private ClientAndServer mockServer;
    private Api solpicJdkHcApi;
    private Api solpicOkHttpApi;
    private Api solpicAhc4Api;
    private Api solpicAhc5Api;

    @Setup
    public void setup() throws Exception {
        String path = "http://localhost:" + PORT + PATH;
        jdkHcApi = new JdkHcApi(path);
        okHttpApi = new OkHttpApi(path);
        ahc4Api = new Ahc4Api(path);
        ahc5Api = new Ahc5Api(path);
        solpicJdkHcApi = new SolpicApi(path, Solpic.newHttpClient("jhc"));
        solpicOkHttpApi = new SolpicApi(path, Solpic.newHttpClient("okhttp"));
        solpicAhc4Api = new SolpicApi(path, Solpic.newHttpClient("ahc4"));
        solpicAhc5Api = new SolpicApi(path, Solpic.newHttpClient("ahc5"));
        mockServer = ClientAndServer.startClientAndServer(PORT);
        mockServer.when(HttpRequest.request().withMethod("GET").withPath(PATH + "/getString"))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.TEXT_PLAIN).withBody("success"));
        mockServer.when(HttpRequest.request().withMethod("POST").withPath(PATH + "/postJson"))
                .respond(HttpResponse.response().withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON).withBody("{\"code\":0,\"message\":\"success\"}"));
        System.out.println(jdkHcApi.getString(QUERY));
        System.out.println(okHttpApi.getString(QUERY));
        System.out.println(ahc4Api.getString(QUERY));
        System.out.println(ahc5Api.getString(QUERY));
        System.out.println(jdkHcApi.postJson(JSON));
        System.out.println(okHttpApi.postJson(JSON));
        System.out.println(ahc4Api.postJson(JSON));
        System.out.println(ahc5Api.postJson(JSON));
        // solpic
        System.out.println(solpicJdkHcApi.getString(QUERY));
        System.out.println(solpicOkHttpApi.getString(QUERY));
        System.out.println(solpicAhc4Api.getString(QUERY));
        System.out.println(solpicAhc5Api.getString(QUERY));
        System.out.println(solpicJdkHcApi.postJson(JSON));
        System.out.println(solpicOkHttpApi.postJson(JSON));
        System.out.println(solpicAhc4Api.postJson(JSON));
        System.out.println(solpicAhc5Api.postJson(JSON));
    }

    @TearDown
    public void tearDown() {
        mockServer.stop();
    }

    @Benchmark
    public void rawJdkHttpClientGet() throws Exception {
        jdkHcApi.getString(QUERY);
    }

    @Benchmark
    public void rawJdkHttpClientPost() throws Exception {
        jdkHcApi.postJson(JSON);
    }

    @Benchmark
    public void rawApacheHttpClientV5Get() throws Exception {
        ahc5Api.getString(QUERY);
    }

    @Benchmark
    public void rawApacheHttpClientV5Post() throws Exception {
        ahc5Api.postJson(JSON);
    }

    @Benchmark
    public void rawApacheHttpClientV4Get() throws Exception {
        ahc4Api.getString(QUERY);
    }

    @Benchmark
    public void rawApacheHttpClientV4Post() throws Exception {
        ahc4Api.postJson(JSON);
    }

    @Benchmark
    public void rawOkHttpGet() throws Exception {
        okHttpApi.getString(QUERY);
    }

    @Benchmark
    public void rawOkHttpPost() throws Exception {
        okHttpApi.postJson(JSON);
    }

    // ############# Solpic ##############

    @Benchmark
    public void solpicJdkHttpClientGet() throws Exception {
        solpicJdkHcApi.getString(QUERY);
    }

    @Benchmark
    public void solpicJdkHttpClientPost() throws Exception {
        solpicJdkHcApi.postJson(JSON);
    }

    @Benchmark
    public void solpicApacheHttpClientV5Get() throws Exception {
        solpicAhc5Api.getString(QUERY);
    }

    @Benchmark
    public void solpicApacheHttpClientV5Post() throws Exception {
        solpicAhc5Api.postJson(JSON);
    }

    @Benchmark
    public void solpicApacheHttpClientV4Get() throws Exception {
        solpicAhc4Api.getString(QUERY);
    }

    @Benchmark
    public void solpicApacheHttpClientV4Post() throws Exception {
        solpicAhc4Api.postJson(JSON);
    }

    @Benchmark
    public void solpicOkHttpGet() throws Exception {
        solpicOkHttpApi.getString(QUERY);
    }

    @Benchmark
    public void solpicOkHttpPost() throws Exception {
        solpicOkHttpApi.postJson(JSON);
    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
                .include(SolpicBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opts).run();
    }
}
