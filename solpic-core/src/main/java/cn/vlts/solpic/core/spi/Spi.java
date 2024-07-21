package cn.vlts.solpic.core.spi;

import java.lang.annotation.*;

/**
 * SPI type annotation.
 *
 * @author throwable
 * @since 2024/7/19 星期五 15:01
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Spi {

    String value() default "";

    boolean singleton() default true;
}
