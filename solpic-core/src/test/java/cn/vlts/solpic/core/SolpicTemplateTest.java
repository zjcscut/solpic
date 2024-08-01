package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.impl.JacksonCodec;
import cn.vlts.solpic.core.http.client.jdk.DefaultJdkHttpClientImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * SolpicTemplateTest.
 *
 * @author throwable
 * @since 2024/7/29 星期一 20:45
 */
public class SolpicTemplateTest {

    @Test
    public void testGetForObject() {
        SolpicTemplate solpicTemplate = Solpic.newSolpicTemplateBuilder()
                .codec(new JacksonCodec<>())
                .httpClient(new DefaultJdkHttpClientImpl())
                .build();
        String result = solpicTemplate.getForObject("https://httpbin.org/get", String.class);
        Assert.assertNotNull(result);
        long start = System.currentTimeMillis();
        result = solpicTemplate.getForObject("https://httpbin.org/get", String.class);
        Assert.assertNotNull(result);
        long end = System.currentTimeMillis();
        System.out.printf("Cost: %d ms\n", end - start);
    }
}
