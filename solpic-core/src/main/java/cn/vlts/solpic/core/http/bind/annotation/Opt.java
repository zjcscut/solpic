package cn.vlts.solpic.core.http.bind.annotation;

import cn.vlts.solpic.core.config.HttpOption;

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

    /**
     * option id
     */
    long id() default -1;

    /**
     * option key
     */
    String key() default "";

    /**
     * option string value
     */
    String value() default "";
}
