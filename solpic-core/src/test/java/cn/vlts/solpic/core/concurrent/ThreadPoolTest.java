package cn.vlts.solpic.core.concurrent;

import cn.vlts.solpic.core.spi.SpiLoader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

/**
 * ThreadPoolTest
 *
 * @author throwable
 * @since 2024/7/21 22:12
 */
public class ThreadPoolTest {

    static SpiLoader<ThreadPool> loader;

    @BeforeClass
    public static void beforeClass() {
        loader = SpiLoader.getSpiLoader(ThreadPool.class);
    }

    @Test
    public void testLoader() {
        Set<String> availableServiceNames = loader.getAvailableServiceNames();
        Assert.assertEquals(2, availableServiceNames.size());
    }

    @Test
    public void testLoadDefault() {
        ThreadPool tp1 = loader.getDefaultService();
        ThreadPool tp2 = loader.getService("default");
        Assert.assertSame(tp1, tp2);
    }

    @Test
    public void testLoadCommon() {
        ThreadPool commonPool = loader.getService("common");
        Assert.assertNotNull(commonPool);
    }


    @Test
    public void testListenableTask() {
        ThreadPool threadPool = loader.getDefaultService();
        ListenableFuture<String> future = threadPool.submit(() -> "helloWorld", new AbstractFutureListener<String>() {
            @Override
            protected void onSuccess(String result) {
                System.out.println("Result callback => " + result);
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {

        }
    }
}
