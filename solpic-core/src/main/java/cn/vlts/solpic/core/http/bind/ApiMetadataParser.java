package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.concurrent.ListenableFuture;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.config.HttpOptionParser;
import cn.vlts.solpic.core.config.HttpOptions;
import cn.vlts.solpic.core.config.OptionLevel;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.bind.annotation.*;
import cn.vlts.solpic.core.http.impl.PayloadPublishers;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.util.ArgumentUtils;
import cn.vlts.solpic.core.util.Pair;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Api metadata parser.
 *
 * @author throwable
 * @since 2024/8/14 23:51
 */
public enum ApiMetadataParser {
    X;

    public ApiMetadata parse(ApiEnhanceSupport enhancerSupport, Class<?> type, Method m) {
        ApiMetadata apiMetadata = new ApiMetadata();
        apiMetadata.setType(type);
        apiMetadata.setMethod(m);
        apiMetadata.setMethodAnnotations(m.getAnnotations());
        apiMetadata.setMethodParameterAnnotations(m.getParameterAnnotations());
        apiMetadata.setParameterTypes(m.getGenericParameterTypes());
        apiMetadata.setReturnType(m.getGenericReturnType());
        // use method parameter annotation array length as parameter count
        apiMetadata.setParameterCount(apiMetadata.getMethodParameterAnnotations().length);
        parseRequestAnnotation(apiMetadata, type, m);
        parseProduceAnnotation(apiMetadata, type, m);
        parseConsumeAnnotation(apiMetadata, type, m);
        parseHttpOptionAnnotation(apiMetadata, type, m);
        parseMethodAnnotations(enhancerSupport, apiMetadata, type, m);
        parseMethodReturnValue(enhancerSupport, apiMetadata, type, m);
        return apiMetadata;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void parseHttpOptionAnnotation(ApiMetadata apiMetadata, Class<?> type, Method method) {
        Opts methodOpts = method.getAnnotation(Opts.class);
        Opts typeOpts = type.getAnnotation(Opts.class);
        Consumer<Opt> optConsumer = opt -> {
            HttpOption httpOption = null;
            if (opt.id() > 0) {
                httpOption = HttpOptions.getById(opt.id());
            } else if (ArgumentUtils.X.hasLength(opt.key())) {
                httpOption = HttpOptions.getByKey(opt.key());
            }
            // only apply request level options
            if (Objects.nonNull(httpOption) && Objects.equals(httpOption.level(), OptionLevel.REQUEST)) {
                Object configValue = Optional.ofNullable(opt.value())
                        .map(httpOption::parseValueFromString)
                        .orElse(null);
                Object optionValue = HttpOptionParser.X.parseOptionValue(httpOption, configValue);
                apiMetadata.addHttpOption(httpOption, optionValue);
            }
        };
        if (Objects.nonNull(typeOpts)) {
            for (Opt typeOpt : typeOpts.value()) {
                optConsumer.accept(typeOpt);
            }
        }
        if (Objects.nonNull(methodOpts)) {
            for (Opt methodOpt : methodOpts.value()) {
                optConsumer.accept(methodOpt);
            }
        }
    }

    private void parseMethodAnnotations(ApiEnhanceSupport enhancerSupport, ApiMetadata apiMetadata, Class<?> type, Method method) {
        Type[] parameterTypes = apiMetadata.getParameterTypes();
        Annotation[][] methodParameterAnnotations = apiMetadata.getMethodParameterAnnotations();
        int c = apiMetadata.getParameterCount();
        RequestParameterHandler<?>[] handlers = new RequestParameterHandler<?>[c];
        for (int i = 0; i < c; i++) {
            RequestParameterHandler<?> requestParameterHandler = parseMethodParameter(apiMetadata, enhancerSupport, i,
                    parameterTypes[i], methodParameterAnnotations[i], method);
            handlers[i] = requestParameterHandler;
        }
        apiMetadata.setRequestParameterHandlers(handlers);
    }

    @SuppressWarnings("unchecked")
    private RequestParameterHandler<?> parseMethodParameter(ApiMetadata apiMetadata,
                                                            ApiEnhanceSupport enhancerSupport,
                                                            int index,
                                                            Type type,
                                                            Annotation[] annotations,
                                                            Method method) {
        if (Objects.nonNull(annotations)) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Form) {
                    Class<?> rawType = ReflectionUtils.X.getRawType(type);
                    if (UrlEncodedForm.class.isAssignableFrom(rawType)) {
                        return new RequestParameterHandler.Form();
                    }
                    if (Map.class.isAssignableFrom(rawType)) {
                        return new RequestParameterHandler.FormMap();
                    }
                    throw new IllegalArgumentException(String.format("Parse @Form error, please check parameters#-%d " +
                            "of type: %s", index, type));
                }
                if (annotation instanceof Multipart) {
                    Class<?> rawType = ReflectionUtils.X.getRawType(type);
                    if (MultipartData.class.isAssignableFrom(rawType)) {
                        return new RequestParameterHandler.Multipart();
                    }
                    throw new IllegalArgumentException(String.format("Parse @Multipart error, please check parameters#-%d " +
                            "of type: %s", index, type));
                }
                if (annotation instanceof Header) {
                    Header header = (Header) annotation;
                    Class<?> rawType = ReflectionUtils.X.getRawType(type);
                    if (!String.class.isAssignableFrom(rawType)) {
                        throw new IllegalArgumentException("Invalid type to apply header: " + type);
                    }
                    return new RequestParameterHandler.Header(header.value());
                }
                if (annotation instanceof Headers) {
                    Class<?> rawType = ReflectionUtils.X.getRawType(type);
                    // header map
                    if (Map.class.isAssignableFrom(rawType)) {
                        return new RequestParameterHandler.HeaderMap();
                    }
                    // iterable http header
                    if (Iterable.class.isAssignableFrom(rawType)) {
                        if (!(type instanceof ParameterizedType)) {
                            throw new IllegalArgumentException("Invalid type to apply headers: " + type);
                        }
                        Type elementType = ReflectionUtils.X.getParameterizedIndexType(0, (ParameterizedType) type);
                        if (elementType instanceof Class && HttpHeader.class.isAssignableFrom((Class) elementType)) {
                            return new RequestParameterHandler.Headers();
                        }
                    }
                    throw new IllegalArgumentException(String.format("Parse @Headers error, please check parameters#-%d " +
                            "of type: %s", index, type));
                }
                if (annotation instanceof Query) {
                    Class<?> rawType = ReflectionUtils.X.getRawType(type);
                    if (String.class.isAssignableFrom(rawType)) {
                        Query query = (Query) annotation;
                        String queryName = query.value();
                        boolean encoded = query.encoded();
                        return new RequestParameterHandler.Query(queryName, encoded);
                    }
                    throw new IllegalArgumentException(String.format("Parse @Query error, please check parameters#-%d " +
                            "of type: %s", index, type));
                }
                if (annotation instanceof Queries) {
                    Class<?> rawType = ReflectionUtils.X.getRawType(type);
                    Queries queries = (Queries) annotation;
                    // query map
                    if (Map.class.isAssignableFrom(rawType)) {
                        return new RequestParameterHandler.QueryMap(queries.encoded());
                    }
                    // iterable query
                    if (Iterable.class.isAssignableFrom(rawType)) {
                        if (!(type instanceof ParameterizedType)) {
                            throw new IllegalArgumentException("Invalid type to apply queries: " + type);
                        }
                        Type elementType = ReflectionUtils.X.getParameterizedIndexType(0, (ParameterizedType) type);
                        if (elementType instanceof Class && Pair.class.isAssignableFrom((Class) elementType)) {
                            return new RequestParameterHandler.Queries(queries.encoded());
                        }
                    }
                    throw new IllegalArgumentException(String.format("Parse @Queries error, please check parameters#-%d " +
                            "of type: %s", index, type));
                }
                if (annotation instanceof Payload) {
                    Class<?> rawType = ReflectionUtils.X.getRawType(type);
                    if (RequestPayloadSupport.class.isAssignableFrom(rawType)) {
                        return new RequestParameterHandler.PayloadSupport();
                    }
                    ApiParameterMetadata apiParameterMetadata = apiMetadata.newApiParameterMetadata(index);
                    Converter<?, RequestPayloadSupport> converter;
                    if (enhancerSupport.supportRequestPayloadConverter(apiParameterMetadata) &&
                            Objects.nonNull(converter = enhancerSupport.getRequestPayloadConverter(apiParameterMetadata))) {
                        return new RequestParameterHandler.Payload(converter);
                    }
                    ContentType produce = apiMetadata.getProduce();
                    if (Objects.nonNull(produce) &&
                            produce.hasSameMimeType(ContentType.APPLICATION_JSON) &&
                            Objects.nonNull(enhancerSupport.getCodec())) {
                        return new RequestParameterHandler.Payload(s -> enhancerSupport.getCodec().createPayloadPublisher(s));
                    }
                    if (PayloadPublishers.X.containsPayloadPublisher(rawType)) {
                        Function<Object, PayloadPublisher> payloadPublisher = PayloadPublishers.X.getPayloadPublisher(rawType);
                        return new RequestParameterHandler.Payload(payloadPublisher::apply);
                    }
                    throw new IllegalArgumentException(String.format("Parse @Payload error, please check parameters#-%d " +
                            "of type: %s", index, type));
                }
                if (annotation instanceof Var) {
                    Var var = (Var) annotation;
                    String varName = var.value();
                    String varDefaultValue = var.defaultValue();
                    if (ArgumentUtils.X.hasLength(varName) && ApiMetadata.ApiVar.exist(varName)) {
                        return new RequestParameterHandler.Var<>(varName, varDefaultValue);
                    }
                    throw new IllegalArgumentException(String.format("Parse @Var error, please check parameters#-%d " +
                            "of type: %s", index, type));
                }
            }
        }
        // ignore parsing parameter
        throw new IllegalArgumentException(String.format("Parse method parameter failed, please check parameters#-%d " +
                "of type: %s", index, type));
    }

    private void parseMethodReturnValue(ApiEnhanceSupport enhancerSupport,
                                        ApiMetadata apiMetadata,
                                        Class<?> type,
                                        Method method) {
        ApiMetadata.SendMode sendMode = ApiMetadata.SendMode.SYNC;
        Type returnType = apiMetadata.getReturnType();
        ReflectionUtils.ParameterizedTypeInfo pti = ReflectionUtils.X.getParameterizedTypeInfo(returnType);
        Class<?> rawReturnType = ReflectionUtils.X.getRawType(returnType);
        apiMetadata.setRawReturnType(rawReturnType);
        boolean hasPayloadSupport = enhancerSupport.supportResponsePayloadSupplier(apiMetadata.newApiReturnMetadata());
        if (!hasPayloadSupport) {
            hasPayloadSupport = PayloadSubscribers.X.containsPayloadSubscriber(returnType);
        }
        if (!hasPayloadSupport) {
            ContentType consume = apiMetadata.getConsume();
            if (Objects.nonNull(consume) &&
                    consume.hasSameMimeType(ContentType.APPLICATION_JSON) &&
                    Objects.nonNull(enhancerSupport.getCodec())) {
                hasPayloadSupport = true;
            }
        }
        boolean wrapByHttpResponse = false;
        // sync mode
        if (hasPayloadSupport && Objects.equals(pti.getRawClass(1, 0), HttpResponse.class)) {
            // just like HttpResponse or HttpResponse<?>
            if (1 == pti.getMaxDepth() && Objects.equals(HttpResponse.class, pti.getRawClass(1, 0))) {
                hasPayloadSupport = false;
            } else if (2 <= pti.getMaxDepth()) {
                // just like HttpResponse<T> or others
                if (Objects.equals(HttpResponse.class, pti.getRawClass(1, 0))) {
                    wrapByHttpResponse = true;
                }
            }
        }
        // async mode
        if (hasPayloadSupport && CompletableFuture.class.isAssignableFrom(rawReturnType)) {
            // just like CompletableFuture or CompletableFuture<?>
            if (1 == pti.getMaxDepth() && Objects.equals(CompletableFuture.class, pti.getRawClass(1, 0))) {
                hasPayloadSupport = false;
            } else if (2 <= pti.getMaxDepth()) {
                // just like CompletableFuture<HttpResponse<T>>
                if (Objects.equals(CompletableFuture.class, pti.getRawClass(1, 0)) &&
                        Objects.equals(HttpResponse.class, pti.getRawClass(2, 0))) {
                    wrapByHttpResponse = true;
                } else {
                    // just like CompletableFuture<T> or others
                    wrapByHttpResponse = false;
                }
            }
            sendMode = ApiMetadata.SendMode.ASYNC;
        }
        // enqueue mode
        if (hasPayloadSupport && ListenableFuture.class.isAssignableFrom(rawReturnType)) {
            // just like ListenableFuture or ListenableFuture<?>
            if (1 == pti.getMaxDepth() && Objects.equals(ListenableFuture.class, pti.getRawClass(1, 0))) {
                hasPayloadSupport = false;
            } else if (2 <= pti.getMaxDepth()) {
                // just like ListenableFuture<HttpResponse<T>>
                if (Objects.equals(ListenableFuture.class, pti.getRawClass(1, 0)) &&
                        Objects.equals(HttpResponse.class, pti.getRawClass(2, 0))) {
                    wrapByHttpResponse = true;
                } else {
                    // just like ListenableFuture<T> or others
                    wrapByHttpResponse = false;
                }
            }
            sendMode = ApiMetadata.SendMode.ENQUEUE;
        }
        // scheduled mode
        if (hasPayloadSupport && ScheduledFuture.class.isAssignableFrom(rawReturnType)) {
            // just like  ScheduledFuture or ScheduledFuture<?>
            if (1 == pti.getMaxDepth() && Objects.equals(ScheduledFuture.class, pti.getRawClass(1, 0))) {
                hasPayloadSupport = false;
            } else if (2 <= pti.getMaxDepth()) {
                // just like ScheduledFuture<HttpResponse<T>>
                if (Objects.equals(ScheduledFuture.class, pti.getRawClass(1, 0)) &&
                        Objects.equals(HttpResponse.class, pti.getRawClass(2, 0))) {
                    wrapByHttpResponse = true;
                } else {
                    // just like ScheduledFuture<T> or others
                    wrapByHttpResponse = false;
                }
            }
            sendMode = ApiMetadata.SendMode.SCHEDULED;
        }
        if (void.class.isAssignableFrom(rawReturnType) || Void.class.isAssignableFrom(rawReturnType)) {
            hasPayloadSupport = false;
        }
        apiMetadata.setSendMode(sendMode);
        apiMetadata.setWrapResponse(wrapByHttpResponse);
        apiMetadata.setHasResponsePayload(hasPayloadSupport);
    }
}
