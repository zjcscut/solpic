package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.Solpic;
import cn.vlts.solpic.core.concurrent.AbstractFutureListener;
import cn.vlts.solpic.core.concurrent.ListenableFuture;
import cn.vlts.solpic.core.http.HttpBinResult;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.util.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

/**
 * Api builder test.
 *
 * @author throwable
 * @since 2024/8/18 23:30
 */
public class ApiEnhancerBuilderTest {

    @Test
    public void testReflection1() throws Exception {
        Method m = HttpBinApi.class.getDeclaredMethod("asyncGetForResponseObject", String.class);
        Type genericReturnType = m.getGenericReturnType();
        System.out.println(genericReturnType);
        ReflectionUtils.ParameterizedTypeInfo pti = ReflectionUtils.X.getParameterizedTypeInfo(genericReturnType);
        System.out.println(pti);
        Class<?> t = pti.getRawClass(3, 0);
        System.out.println(t);
        pti = ReflectionUtils.X.getParameterizedTypeInfo(
                HttpBinApi.class.getDeclaredMethod("asyncGetForObject", String.class).getGenericReturnType()
        );
        System.out.println(pti);
        t = pti.getRawClass(2, 0);
        System.out.println(t);
    }

    @Test
    public void testReflection2() throws Exception {
        ReflectionUtils.ParameterizedTypeInfo pti;
        pti = ReflectionUtils.X.getParameterizedTypeInfo(
                HttpBinApi.class.getDeclaredMethod("rawResponse", String.class).getGenericReturnType()
        );
        System.out.println(pti);
        pti = ReflectionUtils.X.getParameterizedTypeInfo(
                HttpBinApi.class.getDeclaredMethod("responseMap", String.class).getGenericReturnType()
        );
        System.out.println(pti);
    }

    @Test
    public void testHttpBinApiGetString() throws Exception {
        ApiEnhancer apiEnhancer = Solpic.newApiEnhancerBuilder()
                .baseUrl("https://httpbin.org")
                .addConverterFactory(new HttpBinConverterFactory())
                .build();
        HttpBinApi httpBinApi = apiEnhancer.enhance(HttpBinApi.class);
        Assert.assertNotNull(httpBinApi);
        String sr = httpBinApi.getString("bar");
        System.out.println(sr);
        HttpBinResult result = httpBinApi.getForObject("bar");
        System.out.println("===== HttpBinResult =====");
        System.out.println(result);
        HttpResponse<HttpBinResult> hr = httpBinApi.getForResponseObject("bar");
        System.out.println("===== HttpResponse<HttpBinResult> =====");
        System.out.println(hr);
        System.out.println(hr.getPayload());
        CompletableFuture<HttpResponse<HttpBinResult>> ahr = httpBinApi.asyncGetForResponseObject("bar");
        hr = ahr.get();
        System.out.println("===== CompletableFuture<HttpResponse<HttpBinResult>> =====");
        System.out.println(hr);
        System.out.println(hr.getPayload());
    }

    @Test
    public void testEnqueueApi() throws Exception {
        ApiEnhancer apiEnhancer = Solpic.newApiEnhancerBuilder()
                .baseUrl("https://httpbin.org")
                .addConverterFactory(new HttpBinConverterFactory())
                .build();
        HttpBinApi httpBinApi = apiEnhancer.enhance(HttpBinApi.class);
        Assert.assertNotNull(httpBinApi);
        ListenableFuture<?> lf = httpBinApi.enqueueRawGet("bar", new AbstractFutureListener<>() {

            @Override
            protected void onSuccess(Object result) {
                System.out.println("enqueueRawGet.onSuccess, result = " + result);
            }

            @Override
            protected void onError(Throwable throwable) {
                System.out.println("enqueueRawGet.onError, message = " + throwable.getMessage());
            }

            @Override
            protected void onCancel() {
                System.out.println("enqueueRawGet.onCancel");
            }
        });
        lf.get();
        ListenableFuture<HttpBinResult> lof = httpBinApi.enqueueGetForObject("bar", new AbstractFutureListener<>() {

            @Override
            protected void onSuccess(HttpBinResult result) {
                System.out.println("enqueueGetForObject.onSuccess, result = " + result);
            }

            @Override
            protected void onError(Throwable throwable) {
                System.out.println("enqueueGetForObject.onError, message = " + throwable.getMessage());
            }

            @Override
            protected void onCancel() {
                System.out.println("enqueueGetForObject.onCancel");
            }
        });
        lof.get();
        ListenableFuture<HttpResponse<HttpBinResult>> lhof = httpBinApi.enqueueGetForResponseObject("bar",
                new AbstractFutureListener<>() {
                    @Override
                    protected void onSuccess(HttpResponse<HttpBinResult> result) {
                        System.out.println("enqueueGetForResponseObject.onSuccess, response status = " + result.getStatusCode());
                        HttpBinResult payload = result.getPayload();
                        System.out.println("enqueueGetForResponseObject.onSuccess, result = " + payload);
                    }

                    @Override
                    protected void onError(Throwable throwable) {
                        System.out.println("enqueueGetForResponseObject.onError, message = " + throwable.getMessage());
                    }

                    @Override
                    protected void onCancel() {
                        System.out.println("enqueueGetForResponseObject.onCancel");
                    }
                });
        lhof.get();
    }

    @Test
    public void testScheduleApi() throws Exception {
        long delay = 500L;
        ApiEnhancer apiEnhancer = Solpic.newApiEnhancerBuilder()
                .baseUrl("https://httpbin.org")
                .addConverterFactory(new HttpBinConverterFactory())
                .build();
        HttpBinApi httpBinApi = apiEnhancer.enhance(HttpBinApi.class);
        Assert.assertNotNull(httpBinApi);
        CompletableFuture<?> p1 = new CompletableFuture<>();
        p1.whenComplete((r, e) -> {
            if (Objects.nonNull(e)) {
                System.out.println("scheduledRawGet on error, message = " + e.getMessage());
            } else {
                System.out.println("scheduledRawGet on success, result = " + r);
            }
        });
        ScheduledFuture<?> sf = httpBinApi.scheduledRawGet("bar", delay, p1);
        sf.get();
        CompletableFuture<HttpBinResult> p2 = new CompletableFuture<>();
        p2.whenComplete((r, e) -> {
            if (Objects.nonNull(e)) {
                System.out.println("scheduledGetForObject on error, message = " + e.getMessage());
            } else {
                System.out.println("scheduledGetForObject on success, result = " + r);
            }
        });
        ScheduledFuture<HttpBinResult> sof = httpBinApi.scheduledGetForObject("bar", delay, p2);
        sof.get();
        CompletableFuture<HttpResponse<HttpBinResult>> p3 = new CompletableFuture<>();
        p3.whenComplete((r, e) -> {
            if (Objects.nonNull(e)) {
                System.out.println("scheduledGetForResponseObject on error, message = " + e.getMessage());
            } else {
                System.out.println("scheduledGetForResponseObject on success, result = " + r.getStatusCode());
                System.out.println("scheduledGetForResponseObject on success, payload = " + r.getPayload());
            }
        });
        ScheduledFuture<HttpResponse<HttpBinResult>> sorf = httpBinApi.scheduledGetForResponseObject("bar", delay, p3);
        sorf.get();
    }

    @Test
    public void testAsyncGetForResponseObject() throws Exception {
        ApiEnhancer apiEnhancer = Solpic.newApiEnhancerBuilder()
                .baseUrl("https://httpbin.org")
                .addConverterFactory(new HttpBinConverterFactory())
                .build();
        HttpBinApi httpBinApi = apiEnhancer.enhance(HttpBinApi.class);
        CompletableFuture<HttpResponse<HttpBinResult>> r = httpBinApi.asyncGetForResponseObject("bar");
        HttpResponse<HttpBinResult> response = r.get();
        System.out.println(response.getPayload());
    }
}
