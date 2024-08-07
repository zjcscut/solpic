package cn.vlts.solpic.core.http.bind.annotation;

import java.lang.annotation.*;

/**
 * DELETE binding annotation.
 *
 * @author throwable
 * @since 2024/8/8 00:52
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Delete {

    String url() default "";

    String path() default "";
}
