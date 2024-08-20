package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.concurrent.FutureListener;
import cn.vlts.solpic.core.config.HttpOption;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.http.HttpMethod;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    private int parameterCount;

    private Annotation[] methodAnnotations;

    private Annotation[][] methodParameterAnnotations;

    private Type[] parameterTypes;

    private Type returnType;

    private Class<?> rawReturnType;

    private String path;

    private String absoluteUrl;

    private HttpMethod httpMethod;

    private ContentType produce;

    private ContentType consume;

    private final Map<HttpOption<?>, Object> options = new HashMap<>();

    private RequestParameterHandler<?>[] requestParameterHandlers;

    private SendMode sendMode = SendMode.SYNC;

    private boolean wrapResponse = false;

    private boolean hasResponsePayload = true;

    public <T> void addHttpOption(HttpOption<T> option, T value) {
        options.put(option, value);
    }

    public void setRequestParameterHandlers(RequestParameterHandler<?>[] requestParameterHandlers) {
        this.requestParameterHandlers = requestParameterHandlers;
    }

    public ApiParameterMetadata newApiReturnMetadata() {
        return newApiParameterMetadata(-1);
    }

    public ApiParameterMetadata newApiParameterMetadata(int parameterIndex) {
        // return type metadata
        if (parameterIndex == -1) {
            return new DefaultApiParameterMetadata(
                    type,
                    method.getName(),
                    -1,
                    -1,
                    methodAnnotations,
                    null,
                    null,
                    returnType
            );
        }
        if (parameterCount == 0) {
            throw new IllegalStateException("Except parameter index: " + parameterIndex +
                    ", actual parameter count: " + parameterIndex);
        }
        // parameter metadata
        return new DefaultApiParameterMetadata(
                type,
                method.getName(),
                parameterIndex,
                parameterCount,
                methodAnnotations,
                methodParameterAnnotations[parameterIndex],
                parameterTypes[parameterIndex],
                returnType
        );
    }

    public enum SendMode {
        SYNC,

        ASYNC,

        ENQUEUE,

        SCHEDULED
    }

    @RequiredArgsConstructor
    @Getter
    public enum ApiVar {

        DELAY("delay", Long.class, Long::parseLong),

        LISTENER("listener", FutureListener.class, s -> null),

        PROMISE("promise", CompletableFuture.class, s -> null),

        ;

        private final String varName;

        private final Class<?> type;

        private final Function<String, Object> defaultFunction;

        public static ApiVar fromVarName(String varName) {
            for (ApiVar apiVar : ApiVar.values()) {
                if (Objects.equals(varName, apiVar.getVarName())) {
                    return apiVar;
                }
            }
            return null;
        }

        public static boolean exist(String varName) {
            for (ApiVar apiVar : ApiVar.values()) {
                if (Objects.equals(varName, apiVar.getVarName())) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class DefaultApiParameterMetadata implements ApiParameterMetadata {

        private final Class<?> type;

        private final String methodName;

        private final int parameterIndex;

        private final int parameterCount;

        private final Annotation[] methodAnnotations;

        private final Annotation[] parameterAnnotations;

        private final Type parameterType;

        private final Type returnType;

        public DefaultApiParameterMetadata(Class<?> type,
                                           String methodName,
                                           int parameterIndex,
                                           int parameterCount,
                                           Annotation[] methodAnnotations,
                                           Annotation[] parameterAnnotations,
                                           Type parameterType,
                                           Type returnType) {
            this.type = type;
            this.methodName = methodName;
            this.parameterIndex = parameterIndex;
            this.parameterCount = parameterCount;
            this.methodAnnotations = methodAnnotations;
            this.parameterAnnotations = parameterAnnotations;
            this.parameterType = parameterType;
            this.returnType = returnType;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public String getMethodName() {
            return methodName;
        }

        @Override
        public int getParameterIndex() {
            return parameterIndex;
        }

        @Override
        public int getParameterCount() {
            return parameterCount;
        }

        @Override
        public Annotation[] getMethodAnnotations() {
            return methodAnnotations;
        }

        @Override
        public Annotation[] getParameterAnnotations() {
            return parameterAnnotations;
        }

        @Override
        public Type getParameterType() {
            return parameterType;
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }
    }
}
