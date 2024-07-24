package cn.vlts.solpic.core.spi;

/**
 * Default SPI loading strategy.
 *
 * @author throwable
 * @since 2024/7/20 00:11
 */
public class DefaultLoadingStrategy implements LoadingStrategy {

    private static final String NAME = "DEFAULT";

    private static final String LOCATION = "META-INF/solpic/";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String location() {
        return LOCATION;
    }

    @Override
    public boolean overridden() {
        return true;
    }
}
