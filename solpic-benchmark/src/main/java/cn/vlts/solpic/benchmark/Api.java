package cn.vlts.solpic.benchmark;

/**
 * Benchmark API.
 *
 * @author throwable
 * @since 2024/9/22 11:36
 */
public interface Api {

    String getString(String query) throws Exception;

    String postJson(String json) throws Exception;
}
