package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.bind.annotation.Opt;
import cn.vlts.solpic.core.util.Pair;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Api metadata.
 *
 * @author throwable
 * @since 2024/8/14 星期三 20:39
 */
public class ApiMetadata {

    private Class<?> type;

    private String baseUrl;

    private String path;

    private String absoluteUrl;

    private Method method;

    private HttpMethod httpMethod;

    private ContentType produce;

    private ContentType consume;

    private List<Opt> opts;

    private List<Pair> queries;

    private List<Pair> headers;
}
