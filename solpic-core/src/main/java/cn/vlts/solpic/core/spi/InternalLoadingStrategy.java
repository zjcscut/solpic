package cn.vlts.solpic.core.spi;

/**
 * Internal SPI loading strategy.
 *
 * @author throwable
 * @since 2024/7/20 00:11
 */
public class InternalLoadingStrategy implements LoadingStrategy {

    private static final String NAME = "INTERNAL";

    private static final String LOCATION = "META-INF/solpic/internal/";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String location() {
        return LOCATION;
    }
}
