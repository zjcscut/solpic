package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.*;
import cn.vlts.solpic.core.http.client.HttpClientFactory;
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
 * Default api builder.
 *
 * @author throwable
 * @since 2024/8/14 23:54
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultApiBuilder implements ApiBuilder {

    private final Object[] noneArgs = new Object[0];

    private boolean loadEagerly = false;

    private String baseUrl;

    private HttpClient httpClient;

    private Codec codec;

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
        ArgumentUtils.X.notNull("promise", promise);
        this.promise = promise;
        return this;
    }

    @Override
    public ApiBuilder listener(Supplier<FutureListener> listener) {
        ArgumentUtils.X.notNull("listener", listener);
        this.listener = listener;
        return this;
    }

    @Override
    public ApiBuilder httpClient(HttpClient httpClient) {
        ArgumentUtils.X.notNull("httpClient", httpClient);
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public ApiBuilder codec(Codec codec) {
        ArgumentUtils.X.notNull("codec", codec);
        this.codec = codec;
        return this;
    }

    @Override
    public ApiBuilder addConverterFactory(ConverterFactory converterFactory) {
        ArgumentUtils.X.notNull("converterFactory", converterFactory);
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


    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Codec getCodec() {
        return codec;
    }

    public boolean supportRequestPayloadConverter(ApiParameterMetadata metadata) {
        return converterFactories.stream().anyMatch(cf -> cf.supportRequestConverter(metadata));
    }

    public <S> Converter<S, RequestPayloadSupport> getRequestPayloadConverter(ApiParameterMetadata metadata) {
        for (ConverterFactory converterFactory : converterFactories) {
            Converter converter = converterFactory.newRequestConverter(metadata);
            if (Objects.nonNull(converter)) {
                return (Converter<S, RequestPayloadSupport>) converter;
            }
        }
        return null;
    }

    public boolean supportResponsePayloadSupplier(ApiParameterMetadata metadata) {
        return converterFactories.stream().anyMatch(cf -> cf.supportResponseSupplier(metadata));
    }

    public <T> ResponsePayloadSupport<T> getResponsePayloadSupplier(ApiParameterMetadata metadata) {
        for (ConverterFactory converterFactory : converterFactories) {
            Supplier<ResponsePayloadSupport<T>> supplier = converterFactory.newResponseSupplier(metadata);
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
            ApiParameterMetadata returnMetadata = apiMetadata.newApiReturnMetadata();
            ResponsePayloadSupport<?> responsePayloadSupport = getResponsePayloadSupplier(returnMetadata);
            ContentType consume = apiMetadata.getConsume();
            if (Objects.isNull(responsePayloadSupport)) {
                if (PayloadSubscribers.X.containsPayloadSubscriber(apiMetadata.getRawReturnType())) {
                    responsePayloadSupport = PayloadSubscribers.X.getPayloadSubscriber(apiMetadata.getRawReturnType());
                } else if (Objects.nonNull(consume) &&
                        consume.hasSameMimeType(ContentType.APPLICATION_JSON) &&
                        Objects.nonNull(getCodec())) {
                    responsePayloadSupport = getCodec().createPayloadSubscriber(apiMetadata.getRawReturnType());
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
                        listener.get());
                if (apiMetadata.isWrapResponse()) {
                    return httpClient.enqueue(httpRequest, responsePayloadSupport, futureListenerToUse);
                }
                return httpClient.enqueueSimple(httpRequest, responsePayloadSupport, futureListenerToUse);
            }
            // scheduled mode
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
