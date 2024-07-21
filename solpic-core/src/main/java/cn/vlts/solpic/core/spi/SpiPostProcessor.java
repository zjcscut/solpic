package cn.vlts.solpic.core.spi;

/**
 * The SPI post processor.
 *
 * @author throwable
 * @since 2024/7/19 星期五 15:21
 */
public interface SpiPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String name) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String name) {
        return bean;
    }
}
