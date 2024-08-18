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
import java.util.*;
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

    private Class<?> rawResponseType;

    public <T> void addHttpOption(HttpOption<T> option, T value) {
        options.put(option, value);
    }

    public void setRequestParameterHandlers(RequestParameterHandler<?>[] requestParameterHandlers) {
        this.requestParameterHandlers = requestParameterHandlers;
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
}
