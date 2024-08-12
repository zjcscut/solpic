package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.Solpic;
import cn.vlts.solpic.core.SolpicTemplate;
import cn.vlts.solpic.core.http.HttpResponse;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * UrlEncodedForm test.
 *
 * @author throwable
 * @since 2024/8/12 星期一 11:25
 */
public class UrlEncodedFormTest {

    private static SolpicTemplate TEMPLATE;

    @BeforeClass
    public static void before() {
        TEMPLATE = Solpic.newSolpicTemplate();
    }

    @Test
    public void testFormEncoded1() {
        UrlEncodedForm form = UrlEncodedForm.newBuilder().add("a", "avalue").add("b", "bvalue").build();
        HttpResponse<String> r = TEMPLATE.post("http://127.0.0.1:8080/formEncoded1", form, String.class);
        System.out.println(r.getPayload());
        r = TEMPLATE.post("http://127.0.0.1:8080/formEncoded2", form, String.class);
        System.out.println(r.getPayload());
        r = TEMPLATE.post("http://127.0.0.1:8080/formEncoded3", form, String.class);
        System.out.println(r.getPayload());
    }
}
