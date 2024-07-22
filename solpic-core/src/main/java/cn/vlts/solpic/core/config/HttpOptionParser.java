package cn.vlts.solpic.core.config;

import cn.vlts.solpic.core.logging.Logger;
import cn.vlts.solpic.core.logging.LoggerFactory;

import java.util.Objects;

/**
 * HTTP option parser.
 *
 * @author throwable
 * @since 2024/7/22 星期一 16:03
 */
public final class HttpOptionParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpOptionParser.class);

    public static <T> T parseOptionValue(HttpOption<T> option, T configValue) {
        if (Objects.nonNull(configValue)) {
            return configValue;
        }
        String propertyKey = option.propertyKey();
        if (Objects.nonNull(propertyKey)) {
            String propertyValue = System.getProperty(propertyKey);
            if (Objects.nonNull(propertyValue)) {
                try {
                    return option.parseValueFromString(propertyValue);
                } catch (Throwable e) {
                    LOGGER.error("Parse option value failed from property value: " + propertyValue +
                            ", property key: " + propertyKey, e);
                }
            }
        }
        return option.defaultValue();
    }
}
