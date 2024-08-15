package cn.vlts.solpic.core.http.bind.annotation;

import java.lang.annotation.*;

/**
 * Consume, request content type.
 *
 * @author throwable
 * @since 2024/8/15 星期四 10:42
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Consume {

    String value();
}
