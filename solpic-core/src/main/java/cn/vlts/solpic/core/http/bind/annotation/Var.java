package cn.vlts.solpic.core.http.bind.annotation;

import java.lang.annotation.*;

/**
 * Var.
 *
 * @author throwable
 * @since 2024/8/18 15:53
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Var {

    String value() default "";

    String defaultValue() default "";
}
