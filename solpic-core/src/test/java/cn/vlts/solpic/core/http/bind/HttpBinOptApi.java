package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.bind.annotation.Get;
import cn.vlts.solpic.core.http.bind.annotation.Query;

/**
 * httpbin.org api, with opts.
 *
 * @author throwable
 * @since 2024/8/18 23:31
 */
public interface HttpBinOptApi {

    /**
     * baseUrl/get?foo=$bar
     */
    @Get(path = "/get")
    String getString(@Query(value = "foo") String bar);
}
