package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.Solpic;
import cn.vlts.solpic.core.SolpicTemplate;
import cn.vlts.solpic.core.http.HttpResponse;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * MultipartData test.
 *
 * @author throwable
 * @since 2024/8/12 星期一 20:51
 */
public class MultipartDataTest {

    private static SolpicTemplate TEMPLATE;

    @BeforeClass
    public static void before() {
        TEMPLATE = Solpic.newSolpicTemplate();
    }

    @Test
    public void testMultipart() {
        MultipartData multipartData = MultipartData.newBuilder()
                .addTextPart("a", "avalue")
                .addFilePart("b", Paths.get("/Users/throwable/Downloads/银河系.png"))
                .build();
        HttpResponse<String> r = TEMPLATE.post("http://127.0.0.1:8080/multipart1", multipartData, String.class);
        System.out.println(r.getPayload());
    }
}
