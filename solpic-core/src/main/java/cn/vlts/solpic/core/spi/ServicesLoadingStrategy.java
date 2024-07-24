package cn.vlts.solpic.core.spi;

/**
 * Services SPI loading strategy.
 *
 * @author throwable
 * @since 2024/7/20 00:11
 */
public class ServicesLoadingStrategy implements LoadingStrategy {

    private static final String NAME = "SERVICES";

    private static final String LOCATION = "META-INF/services/";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String location() {
        return LOCATION;
    }
}
