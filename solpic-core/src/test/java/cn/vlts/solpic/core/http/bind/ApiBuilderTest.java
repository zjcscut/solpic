package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.Solpic;
import cn.vlts.solpic.core.http.HttpBinResult;
import cn.vlts.solpic.core.http.HttpResponse;
import cn.vlts.solpic.core.util.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * Api builder test.
 *
 * @author throwable
 * @since 2024/8/18 23:30
 */
public class ApiBuilderTest {

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
    public void testHttpBinApiGetString() {
        HttpBinApi httpBinApi = Solpic.newApiBuilder()
                .baseUrl("https://httpbin.org")
                .converterFactory(new HttpBinConverterFactory())
                .build(HttpBinApi.class);
        Assert.assertNotNull(httpBinApi);
        String sr = httpBinApi.getString("bar");
        System.out.println(sr);
        HttpBinResult result = httpBinApi.getForObject("bar");
        System.out.println(result);
    }

    @Test
    public void testAsyncGetForResponseObject() throws Exception{
        HttpBinApi httpBinApi = Solpic.newApiBuilder()
                .baseUrl("https://httpbin.org")
                .converterFactory(new HttpBinConverterFactory())
                .build(HttpBinApi.class);
        CompletableFuture<HttpResponse<HttpBinResult>> r = httpBinApi.asyncGetForResponseObject("bar");
        HttpResponse<HttpBinResult> response = r.get();
        System.out.println(response.getPayload());
    }
}
