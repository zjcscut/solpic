package cn.vlts.solpic.core.http.bind.annotation;

import java.lang.annotation.*;

/**
 * HTTP option.
 *
 * @author throwable
 * @since 2024/8/14 星期三 20:43
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Opts.class)
public @interface Opt {

    long id() default -1;

    String key() default "";

    String value() default "";
}
