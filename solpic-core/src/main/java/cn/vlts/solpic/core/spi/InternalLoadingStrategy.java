package cn.vlts.solpic.core.spi;

/**
 * @author throwable
 * @since 2024/7/20 00:11
 */
public class InternalLoadingStrategy implements LoadingStrategy {

    @Override
    public String name() {
        return "INTERNAL";
    }

    @Override
    public String location() {
        return "META-INF/solpic/internal/";
    }
}
