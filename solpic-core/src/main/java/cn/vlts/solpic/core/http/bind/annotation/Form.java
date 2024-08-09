package cn.vlts.solpic.core.http.bind.annotation;

import java.lang.annotation.*;

/**
 * Url encoded form binding.
 *
 * @author throwable
 * @since 2024/8/9 星期五 15:26
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Form {

    boolean encoded() default true;
}
