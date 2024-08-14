package cn.vlts.solpic.core.http.bind.annotation;

import java.lang.annotation.*;

/**
 * HTTP options.
 *
 * @author throwable
 * @since 2024/8/14 星期三 20:49
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Opts {

    Opt[] value() default {};
}