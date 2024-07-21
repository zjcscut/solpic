package cn.vlts.solpic.core.spi;

/**
 * @author throwable
 * @since 2024/7/20 00:11
 */
public class DefaultLoadingStrategy implements LoadingStrategy {

    @Override
    public String name() {
        return "DEFAULT";
    }

    @Override
    public String location() {
        return "META-INF/solpic/";
    }

    @Override
    public boolean overridden() {
        return true;
    }
}
