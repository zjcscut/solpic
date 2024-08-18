package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.HttpClient;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.RequestPayloadSupport;
import cn.vlts.solpic.core.http.ResponsePayloadSupport;
import cn.vlts.solpic.core.http.client.HttpClientFactory;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.util.ArgumentUtils;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Default api builder.
 *
 * @author throwable
 * @since 2024/8/14 23:54
 */
@SuppressWarnings("unchecked")
public class DefaultApiBuilder implements ApiBuilder {

    private final Object[] noneArgs = new Object[0];

    private boolean loadEagerly = false;

    private String baseUrl;

    private HttpClient httpClient;

    private Long delay = 0L;

    private Supplier<CompletableFuture> promise = CompletableFuture::new;

    private Supplier<FutureListener> listener = () -> f -> {
    };

    private final List<ConverterFactory> converterFactories = new ArrayList<>();

    private final ConcurrentMap<Method, ApiMetadata> apiMetadataCache = new ConcurrentHashMap<>();

    private final ConcurrentMap<Method, MethodHandle> defaultMethodCache = new ConcurrentHashMap<>();

    @Override
    public ApiBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public ApiBuilder loadEagerly() {
        this.loadEagerly = true;
        return this;
    }

    @Override
    public ApiBuilder delay(long delay) {
        ArgumentUtils.X.isTrue(delay > 0, "delay must be greater than 0");
        this.delay = delay;
        return this;
    }

    @Override
    public ApiBuilder promise(Supplier<CompletableFuture> promise) {
        this.promise = promise;
        return this;
    }

    @Override
    public ApiBuilder listener(Supplier<FutureListener> listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public ApiBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public ApiBuilder converterFactory(ConverterFactory converterFactory) {
        converterFactories.add(converterFactory);
        return this;
    }

    @Override
    public <T> T build(Class<T> type) {
        ArgumentUtils.X.notNull("baseUrl", baseUrl);
        validate(type);
        if (Objects.isNull(httpClient)) {
            httpClient = HttpClientFactory.X.loadBestMatchedHttpClient();
        }
        if (isLoadEagerly()) {
            Method[] declaredMethods = type.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (!method.isDefault()
                        && !Modifier.isStatic(method.getModifiers())
                        && !method.isSynthetic()) {
                    apiMetadataCache.computeIfAbsent(method, m -> ApiMetadataParser.X.parse(this, type, m));
                }
            }
        }
        return enhance(type);
    }

    public boolean isLoadEagerly() {
        return loadEagerly;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public <S> Converter<S, RequestPayloadSupport> getRequestPayloadConverter(Type type,
                                                                              Annotation[] parameterAnnotations,
                                                                              Annotation[] methodAnnotations) {
        for (ConverterFactory converterFactory : converterFactories) {
            Converter converter = converterFactory.newRequestConverter(type, parameterAnnotations, methodAnnotations);
            if (Objects.nonNull(converter)) {
                return (Converter<S, RequestPayloadSupport>) converter;
            }
        }
        return null;
    }

    public <T> ResponsePayloadSupport<T> getResponsePayloadSupplier(Type type,
                                                                    Annotation[] methodAnnotations) {
        for (ConverterFactory converterFactory : converterFactories) {
            Supplier<ResponsePayloadSupport<T>> supplier = converterFactory.newResponseSupplier(type, methodAnnotations);
            if (Objects.nonNull(supplier)) {
                return supplier.get();
            }
        }
        return null;
    }

    private void validate(Class<?> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Required interface type");
        }
        Deque<Class<?>> check = new ArrayDeque<>();
        check.add(type);
        while (!check.isEmpty()) {
            Class<?> candidate = check.removeFirst();
            if (candidate.getTypeParameters().length != 0) {
                StringBuilder message =
                        new StringBuilder("Type parameters are unsupported on ").append(candidate.getName());
                if (candidate != type) {
                    message.append(" which is an interface of ").append(type.getName());
                }
                throw new IllegalArgumentException(message.toString());
            }
            Collections.addAll(check, candidate.getInterfaces());
        }
    }

    private <T> T enhance(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (Object.class == method.getDeclaringClass()) {
                return method.invoke(this, args);
            }
            Object[] argsToUse = Objects.nonNull(args) ? args : noneArgs;
            // default method
            if (method.isDefault()) {
                MethodHandle defaultMethodHandle = defaultMethodCache.computeIfAbsent(method,
                        ReflectionUtils.X::getSpecialMethodHandle);
                return defaultMethodHandle.invokeWithArguments(argsToUse);
            }
            ApiMetadata apiMetadata = apiMetadataCache.computeIfAbsent(method,
                    m -> ApiMetadataParser.X.parse(this, type, m));
            ResponsePayloadSupport<?> responsePayloadSupport =
                    getResponsePayloadSupplier(type, apiMetadata.getMethodAnnotations());
            if (Objects.isNull(responsePayloadSupport)) {
                if (PayloadSubscribers.X.containsPayloadSubscriber(apiMetadata.getRawResponseType())) {
                    responsePayloadSupport = PayloadSubscribers.X.getPayloadSubscriber(apiMetadata.getRawResponseType());
                } else if (!apiMetadata.isHasResponsePayload()) {
                    responsePayloadSupport = PayloadSubscribers.X.discarding();
                } else {
                    throw new IllegalArgumentException("Unsupported response payload type, please check converterFactory");
                }
            }
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
            String absoluteUrl = apiMetadata.getAbsoluteUrl();
            if (ArgumentUtils.X.hasLength(absoluteUrl)) {
                requestBuilder.uri(URI.create(absoluteUrl));
            } else {
                requestBuilder.uri(URI.create(baseUrl));
                if (ArgumentUtils.X.hasLength(apiMetadata.getPath())) {
                    requestBuilder.path(apiMetadata.getPath());
                }
            }
            requestBuilder.method(apiMetadata.getHttpMethod());
            Map<HttpOption<?>, Object> options = apiMetadata.getOptions();
            for (Map.Entry<HttpOption<?>, Object> entry : options.entrySet()) {
                HttpOption key = entry.getKey();
                requestBuilder.option(key, entry.getValue());
            }
            // request context vars
            Map<ApiMetadata.ApiVar, Object> vars = new HashMap<>();
            int argumentCount = argsToUse.length;
            RequestParameterHandler<Object>[] handlers = (RequestParameterHandler<Object>[])
                    apiMetadata.getRequestParameterHandlers();
            if (argumentCount != handlers.length) {
                throw new IllegalArgumentException(String.format("Arguments count (%d) does not match " +
                        "expected number of parameters (%d)", argumentCount, handlers.length));
            }
            for (int i = 0; i < argumentCount; i++) {
                Object argument = argsToUse[i];
                RequestParameterHandler<Object> handler = handlers[i];
                handler.apply(() -> argument, requestBuilder);
                if (handler instanceof RequestParameterHandler.Var) {
                    RequestParameterHandler.Var vh = (RequestParameterHandler.Var) handler;
                    vars.put(vh.getVar(), vh.getVarValue());
                }
            }
            HttpRequest httpRequest = requestBuilder.build();
            ApiMetadata.SendMode sendMode = apiMetadata.getSendMode();
            if (ApiMetadata.SendMode.ASYNC == sendMode) {
                if (apiMetadata.isWrapResponse()) {
                    return httpClient.sendAsync(httpRequest, responsePayloadSupport);
                }
                return httpClient.sendAsyncSimple(httpRequest, responsePayloadSupport);
            }
            if (ApiMetadata.SendMode.ENQUEUE == sendMode) {
                FutureListener futureListenerToUse = (FutureListener) vars.getOrDefault(ApiMetadata.ApiVar.LISTENER,
                        listener.get());
                if (apiMetadata.isWrapResponse()) {
                    return httpClient.enqueue(httpRequest, responsePayloadSupport, futureListenerToUse);
                }
                return httpClient.enqueueSimple(httpRequest, responsePayloadSupport, futureListenerToUse);
            }
            if (ApiMetadata.SendMode.SCHEDULED == sendMode) {
                Long delayToUse = (Long) vars.getOrDefault(ApiMetadata.ApiVar.DELAY, delay);
                CompletableFuture promiseToUse = (CompletableFuture) vars.getOrDefault(ApiMetadata.ApiVar.PROMISE,
                        promise.get());
                if (apiMetadata.isWrapResponse()) {
                    return httpClient.scheduledSend(httpRequest, responsePayloadSupport,
                            delayToUse, TimeUnit.MILLISECONDS, promiseToUse);
                }
                return httpClient.scheduledSendSimple(httpRequest, responsePayloadSupport,
                        delayToUse, TimeUnit.MILLISECONDS, promiseToUse);
            }
            // sync mode
            if (apiMetadata.isWrapResponse()) {
                return httpClient.send(httpRequest, responsePayloadSupport);
            }
            return httpClient.sendSimple(httpRequest, responsePayloadSupport);
        });
    }
}
