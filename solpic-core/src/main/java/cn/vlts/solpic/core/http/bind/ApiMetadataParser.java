package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpMethod;
import cn.vlts.solpic.core.http.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Api metadata parser.
 *
 * @author throwable
 * @since 2024/8/14 23:51
 */
public enum ApiMetadataParser {
    X;

    private static final ConcurrentMap<Method, ApiMetadata> API_METADATA_CACHE = new ConcurrentHashMap<>();

    public ApiMetadata parse(DefaultApiBuilder builder, Class<?> type, Method method) {
        return API_METADATA_CACHE.computeIfAbsent(method, m -> {
            ApiMetadata apiMetadata = new ApiMetadata();
            apiMetadata.setType(type);
            apiMetadata.setMethod(m);
            String baseUrl = builder.getBaseUrl();
            apiMetadata.setBaseUrl(baseUrl);
            parseRequestAnnotation(apiMetadata, type, m);
            parseProduceAnnotation(apiMetadata, type, m);
            parseConsumeAnnotation(apiMetadata, type, m);
            return apiMetadata;
        });
    }

    private void parseRequestAnnotation(ApiMetadata apiMetadata, Class<?> type, Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            Get get = method.getAnnotation(Get.class);
            apiMetadata.setHttpMethod(HttpMethod.GET);
            apiMetadata.setPath(get.path());
            apiMetadata.setAbsoluteUrl(get.url());
        } else if (method.isAnnotationPresent(Post.class)) {
            Post post = method.getAnnotation(Post.class);
            apiMetadata.setHttpMethod(HttpMethod.POST);
            apiMetadata.setPath(post.path());
            apiMetadata.setAbsoluteUrl(post.url());
        } else if (method.isAnnotationPresent(Put.class)) {
            Put put = method.getAnnotation(Put.class);
            apiMetadata.setHttpMethod(HttpMethod.PUT);
            apiMetadata.setPath(put.path());
            apiMetadata.setAbsoluteUrl(put.url());
        } else if (method.isAnnotationPresent(Delete.class)) {
            Delete delete = method.getAnnotation(Delete.class);
            apiMetadata.setHttpMethod(HttpMethod.DELETE);
            apiMetadata.setPath(delete.path());
            apiMetadata.setAbsoluteUrl(delete.url());
        } else if (method.isAnnotationPresent(Head.class)) {
            Head head = method.getAnnotation(Head.class);
            apiMetadata.setHttpMethod(HttpMethod.HEAD);
            apiMetadata.setPath(head.path());
            apiMetadata.setAbsoluteUrl(head.url());
        } else if (method.isAnnotationPresent(Patch.class)) {
            Patch patch = method.getAnnotation(Patch.class);
            apiMetadata.setHttpMethod(HttpMethod.PATCH);
            apiMetadata.setPath(patch.path());
            apiMetadata.setAbsoluteUrl(patch.url());
        } else if (method.isAnnotationPresent(Trace.class)) {
            Trace trace = method.getAnnotation(Trace.class);
            apiMetadata.setHttpMethod(HttpMethod.TRACE);
            apiMetadata.setPath(trace.path());
            apiMetadata.setAbsoluteUrl(trace.url());
        } else if (method.isAnnotationPresent(Options.class)) {
            Options options = method.getAnnotation(Options.class);
            apiMetadata.setHttpMethod(HttpMethod.OPTIONS);
            apiMetadata.setPath(options.path());
            apiMetadata.setAbsoluteUrl(options.url());
        } else if (method.isAnnotationPresent(Request.class)) {
            Request request = method.getAnnotation(Request.class);
            apiMetadata.setHttpMethod(request.method());
            apiMetadata.setPath(request.path());
            apiMetadata.setAbsoluteUrl(request.url());
        } else {
            throw new IllegalArgumentException("Request method annotation is required");
        }
    }

    private void parseProduceAnnotation(ApiMetadata apiMetadata, Class<?> type, Method method) {
        Produce methodProduce = method.getAnnotation(Produce.class);
        Produce typeProduce = type.getAnnotation(Produce.class);
        if (Objects.nonNull(methodProduce)) {
            apiMetadata.setProduce(ContentType.parse(methodProduce.value()));
        } else if (Objects.nonNull(typeProduce)) {
            apiMetadata.setProduce(ContentType.parse(typeProduce.value()));
        }
    }

    private void parseConsumeAnnotation(ApiMetadata apiMetadata, Class<?> type, Method method) {
        Consume methodConsume = method.getAnnotation(Consume.class);
        Consume typeConsume = type.getAnnotation(Consume.class);
        if (Objects.nonNull(methodConsume)) {
            apiMetadata.setConsume(ContentType.parse(methodConsume.value()));
        } else if (Objects.nonNull(typeConsume)) {
            apiMetadata.setConsume(ContentType.parse(typeConsume.value()));
        }
    }

    public boolean existApiMetadata(Method method) {
        return API_METADATA_CACHE.containsKey(method);
    }

    public ApiMetadata getApiMetadata(Method method) {
        return API_METADATA_CACHE.get(method);
    }
}
