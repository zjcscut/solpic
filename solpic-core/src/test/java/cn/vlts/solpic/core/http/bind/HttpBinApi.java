package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.HttpBinResult;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.http.bind.annotation.Get;
import cn.vlts.solpic.core.http.bind.annotation.Query;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
}
