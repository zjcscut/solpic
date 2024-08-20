package cn.vlts.solpic.core.http.bind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Api parameter metadata.
 *
 * @author throwable
 * @since 2024/8/19 星期一 15:02
 */
public interface ApiParameterMetadata {

    /**
     * Api interface type.
     */
    Class<?> getType();

    /**
     * Method name.
     */
    String getMethodName();

    /**
     * Current parameter index, -1 means return parameter.
     */
    int getParameterIndex();

    /**
     * Total parameter count, -1 means return parameter.
     */
    int getParameterCount();

    /**
     * Method annotations.
     */
    Annotation[] getMethodAnnotations();

    /**
     * Method parameter annotations, null means return parameter.
     */
    Annotation[] getParameterAnnotations();

    /**
     * Method parameter type, null means return parameter.
     */
    Type getParameterType();

    /**
     * Method return type.
     */
    Type getReturnType();
}
