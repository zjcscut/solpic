package cn.vlts.solpic.core.spi;

import cn.vlts.solpic.core.concurrent.ThreadPool;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.interceptor.HttpInterceptor;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * SPI loader test.
 *
 * @author throwable
 * @since 2024/8/22 星期四 15:09
 */
public class SpiLoaderTest {

    @Test
    public void testLoadInterceptors() {
        Set<String> availableServiceNames = SpiLoader.getSpiLoader(HttpInterceptor.class).getAvailableServiceNames();
        Assert.assertNotNull(availableServiceNames);
        List<HttpInterceptor> availableServices = SpiLoader.getSpiLoader(HttpInterceptor.class).getAvailableServices();
        Assert.assertNotNull(availableServices);
        availableServiceNames = SpiLoader.getSpiLoader(ThreadPool.class).getAvailableServiceNames();
        Assert.assertNotNull(availableServiceNames);
        availableServiceNames = SpiLoader.getSpiLoader(HttpClient.class).getAvailableServiceNames();
        Assert.assertNotNull(availableServiceNames);
        List<HttpClient> httpClientList = SpiLoader.getSpiLoader(HttpClient.class).getAvailableServices();
        Assert.assertNotNull(httpClientList);
    }
}
