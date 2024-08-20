package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.impl.PayloadSubscribers;
import cn.vlts.solpic.core.util.ArgumentUtils;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Default api enhancer.
 *
 * @author throwable
 * @since 2024/8/21 00:08
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class DefaultApiEnhancer extends ApiEnhanceSupport implements ApiEnhancer {

    private final List<ConverterFactory> converterFactoriesCache = new ArrayList<>();

    private final ConcurrentMap<Method, ApiMetadata> apiMetadataCache = new ConcurrentHashMap<>();

    private final ConcurrentMap<Method, MethodHandle> defaultMethodCache = new ConcurrentHashMap<>();

    private final Object[] noneArgs = new Object[0];

    private final boolean loadEagerly;

    private final String baseUrl;

    private final HttpClient httpClient;

    private final Codec codec;

    private final Long defaultDelayMillis;

    private final Supplier<CompletableFuture> promiseSupplier;

    private final Supplier<FutureListener> futureListenerSupplier;

    public DefaultApiEnhancer(boolean loadEagerly,
                              String baseUrl,
                              HttpClient httpClient,
                              Codec codec,
                              Long defaultDelayMillis,
                              Supplier<CompletableFuture> promiseSupplier,
                              Supplier<FutureListener> futureListenerSupplier,
                              List<ConverterFactory> converterFactories) {
        this.loadEagerly = loadEagerly;
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.codec = codec;
        this.defaultDelayMillis = defaultDelayMillis;
        this.promiseSupplier = promiseSupplier;
        this.futureListenerSupplier = futureListenerSupplier;
        if (Objects.nonNull(converterFactories)) {
            this.converterFactoriesCache.addAll(converterFactories);
        }
    }

    @Override
    public <T> T enhance(Class<T> type) {
        validate(type);
        loadEagerlyIfNecessary(type);
        return enhanceApi(type);
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

    private void loadEagerlyIfNecessary(Class<?> type) {
        if (loadEagerly) {
            Method[] declaredMethods = type.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (!method.isDefault()
                        && !Modifier.isStatic(method.getModifiers())
                        && !method.isSynthetic()) {
                    apiMetadataCache.computeIfAbsent(method, m -> ApiMetadataParser.X.parse(this, type, m));
                }
            }
        }
    }

    private <T> T enhanceApi(Class<T> type) {
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
            ApiParameterMetadata returnTypeMetadata = apiMetadata.newApiReturnMetadata();
            ResponsePayloadSupport<?> responsePayloadSupport = getResponsePayloadSupplier(returnTypeMetadata);
            ContentType consume = apiMetadata.getConsume();
            if (Objects.isNull(responsePayloadSupport)) {
                if (PayloadSubscribers.X.containsPayloadSubscriber(apiMetadata.getRawReturnType())) {
                    responsePayloadSupport = PayloadSubscribers.X.getPayloadSubscriber(apiMetadata.getRawReturnType());
                } else if (Objects.nonNull(consume) &&
                        consume.hasSameMimeType(ContentType.APPLICATION_JSON) &&
                        Objects.nonNull(codec)) {
                    responsePayloadSupport = codec.createPayloadSubscriber(apiMetadata.getRawReturnType());
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
            // apply request parameter handlers
            for (int i = 0; i < argumentCount; i++) {
                Object argument = argsToUse[i];
                RequestParameterHandler<Object> handler = handlers[i];
                handler.apply(() -> argument, requestBuilder);
                if (handler instanceof RequestParameterHandler.Var) {
                    RequestParameterHandler.Var vh = (RequestParameterHandler.Var) handler;
                    Object varValue = vh.getVarValue();
                    if (Objects.nonNull(varValue)) {
                        vars.put(vh.getVar(), varValue);
                    }
                }
            }
            HttpRequest httpRequest = requestBuilder.build();
            ApiMetadata.SendMode sendMode = apiMetadata.getSendMode();
            // async mode
            if (ApiMetadata.SendMode.ASYNC == sendMode) {
                if (apiMetadata.isWrapResponse()) {
                    return httpClient.sendAsync(httpRequest, responsePayloadSupport);
                }
                return httpClient.sendAsyncSimple(httpRequest, responsePayloadSupport);
            }
            // enqueue mode
            if (ApiMetadata.SendMode.ENQUEUE == sendMode) {
                FutureListener futureListenerToUse = (FutureListener) vars.getOrDefault(ApiMetadata.ApiVar.LISTENER,
                        futureListenerSupplier.get());
                if (apiMetadata.isWrapResponse()) {
                    return httpClient.enqueue(httpRequest, responsePayloadSupport, futureListenerToUse);
                }
                return httpClient.enqueueSimple(httpRequest, responsePayloadSupport, futureListenerToUse);
            }
            // scheduled mode
            if (ApiMetadata.SendMode.SCHEDULED == sendMode) {
                Long delayToUse = (Long) vars.getOrDefault(ApiMetadata.ApiVar.DELAY, defaultDelayMillis);
                CompletableFuture promiseToUse = (CompletableFuture) vars.getOrDefault(ApiMetadata.ApiVar.PROMISE,
                        promiseSupplier.get());
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

    @Override
    public boolean supportRequestPayloadConverter(ApiParameterMetadata metadata) {
        return converterFactoriesCache.stream().anyMatch(cf -> cf.supportRequestConverter(metadata));
    }

    @Override
    public <S> Converter<S, RequestPayloadSupport> getRequestPayloadConverter(ApiParameterMetadata metadata) {
        for (ConverterFactory converterFactory : converterFactoriesCache) {
            Converter converter = converterFactory.newRequestConverter(metadata);
            if (Objects.nonNull(converter)) {
                return (Converter<S, RequestPayloadSupport>) converter;
            }
        }
        return null;
    }

    @Override
    public boolean supportResponsePayloadSupplier(ApiParameterMetadata metadata) {
        return converterFactoriesCache.stream().anyMatch(cf -> cf.supportResponseSupplier(metadata));
    }

    @Override
    public <T> ResponsePayloadSupport<T> getResponsePayloadSupplier(ApiParameterMetadata metadata) {
        for (ConverterFactory converterFactory : converterFactoriesCache) {
            Supplier<ResponsePayloadSupport<T>> supplier = converterFactory.newResponseSupplier(metadata);
            if (Objects.nonNull(supplier)) {
                return supplier.get();
            }
        }
        return null;
    }

    @Override
    public Codec getCodec() {
        return codec;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
