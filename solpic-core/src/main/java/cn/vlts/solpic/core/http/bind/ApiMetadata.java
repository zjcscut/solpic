package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.util.Pair;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Api metadata.
 *
 * @author throwable
 * @since 2024/8/14 星期三 20:39
 */
@Data
public class ApiMetadata {

    private Class<?> type;

    private Method method;

    private String baseUrl;

    private String path;

    private String absoluteUrl;

    private HttpMethod httpMethod;

    private ContentType produce;

    private ContentType consume;

    private final List<HttpOption<?>> options = new ArrayList<>();

    private final List<Pair> queries = new ArrayList<>();

    private final List<Pair> headers = new ArrayList<>();
}
