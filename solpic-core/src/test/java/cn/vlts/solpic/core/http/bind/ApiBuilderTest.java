package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.Solpic;
import org.junit.Assert;
import org.junit.Test;

/**
 * Api builder test.
 *
 * @author throwable
 * @since 2024/8/18 23:30
 */
public class ApiBuilderTest {

    @Test
    public void testHttpBinApiGetString() {
        HttpBinApi httpBinApi = Solpic.newApiBuilder()
                .baseUrl("https://httpbin.org")
                .build(HttpBinApi.class);
        Assert.assertNotNull(httpBinApi);
        String r = httpBinApi.getString("bar");
        System.out.println(r);
    }
}
