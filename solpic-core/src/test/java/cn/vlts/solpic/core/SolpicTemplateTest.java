package cn.vlts.solpic.core;

import cn.vlts.solpic.core.codec.impl.JacksonCodec;
import cn.vlts.solpic.core.http.client.jdk.JdkHttpClientImpl;
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
                .httpClient(new JdkHttpClientImpl())
                .build();
        String result = solpicTemplate.getForObject("https://httpbin.org/get", String.class);
        Assert.assertNotNull(result);
    }
}
