package cn.vlts.solpic.core.http;

import org.junit.Assert;
import org.junit.Test;

/**
 * ContentTypeTest.
 *
 * @author throwable
 * @since 2024/7/23 星期二 16:44
 */
public class ContentTypeTest {

    @Test
    public void testParseContentType() {
        String contentType = "application/json; charset=UTF-8; a=b; c=d;";
        ContentType c1 = ContentType.parse(contentType);
        ContentType c2 = ContentType.parse(contentType);
        Assert.assertSame(c1, c2);
    }
}
