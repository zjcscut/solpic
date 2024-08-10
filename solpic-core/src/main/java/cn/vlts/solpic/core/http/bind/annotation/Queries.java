package cn.vlts.solpic.core.http.bind.annotation;

import java.lang.annotation.*;

/**
 * Request query parameters binding.
 *
 * @author throwable
 * @since 2024/8/10 14:21
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Queries {

    boolean encoded() default false;
}
