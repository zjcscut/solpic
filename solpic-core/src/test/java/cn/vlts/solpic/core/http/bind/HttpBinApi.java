package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;
import cn.vlts.solpic.core.http.HttpBinResult;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.bind.annotation.Get;
import cn.vlts.solpic.core.http.bind.annotation.Query;
import cn.vlts.solpic.core.http.bind.annotation.Var;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

/**
 * httpbin.org api.
 *
 * @author throwable
 * @since 2024/8/18 23:31
 */
public interface HttpBinApi {

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    HttpResponse<?> rawResponse(@Query(value = "foo") String bar);


    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    HttpResponse<Map<String, Object>> responseMap(@Query(value = "foo") String bar);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    String getString(@Query(value = "foo") String bar);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    HttpBinResult getForObject(@Query(value = "foo") String bar);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    HttpResponse<HttpBinResult> getForResponseObject(@Query(value = "foo") String bar);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    CompletableFuture<HttpBinResult> asyncGetForObject(@Query(value = "foo") String bar);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    CompletableFuture<HttpResponse<HttpBinResult>> asyncGetForResponseObject(@Query(value = "foo") String bar);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    ListenableFuture<?> enqueueRawGet(@Query(value = "foo") String bar,
                                      @Var("listener") FutureListener<?> listener);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    ListenableFuture<HttpBinResult> enqueueGetForObject(@Query(value = "foo") String bar,
                                                        @Var("listener") FutureListener<HttpBinResult> listener);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    ListenableFuture<HttpResponse<HttpBinResult>> enqueueGetForResponseObject(@Query(value = "foo") String bar,
                                                                              @Var("listener") FutureListener<HttpResponse<HttpBinResult>> listener);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    ScheduledFuture<?> scheduledRawGet(@Query(value = "foo") String bar,
                                       @Var("delay") Long delay,
                                       @Var("promise") CompletableFuture<?> promise);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    ScheduledFuture<HttpBinResult> scheduledGetForObject(@Query(value = "foo") String bar,
                                                          @Var("delay") Long delay,
                                                          @Var("promise") CompletableFuture<HttpBinResult> promise);

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    ScheduledFuture<HttpResponse<HttpBinResult>> scheduledGetForResponseObject(@Query(value = "foo") String bar,
                                                                                @Var("delay") Long delay,
                                                                                @Var("promise") CompletableFuture<HttpResponse<HttpBinResult>> promise);
}
