package cn.vlts.solpic.core.spi;

/**
 * @author throwable
 * @since 2024/7/20 00:11
 */
public class ServicesLoadingStrategy implements LoadingStrategy {

    @Override
    public String name() {
        return "SERVICES";
    }

    @Override
    public String location() {
        return "META-INF/services/";
    }
}
