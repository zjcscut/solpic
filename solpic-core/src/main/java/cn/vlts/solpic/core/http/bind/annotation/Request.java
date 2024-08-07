package cn.vlts.solpic.core.http.bind.annotation;

import cn.vlts.solpic.core.http.HttpMethod;

import java.lang.annotation.*;

/**
 * Request binding annotation.
 *
 * @author throwable
 * @since 2024/8/8 00:46
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Request {

    HttpMethod method() default HttpMethod.GET;

    String url() default "";

    String path() default "";
}
