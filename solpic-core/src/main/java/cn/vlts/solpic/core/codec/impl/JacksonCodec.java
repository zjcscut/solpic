package cn.vlts.solpic.core.codec.impl;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.util.ReflectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Jackson codec.
 *
 * @author throwable
 * @since 2024/7/27 15:44
 */
public class JacksonCodec<S, T> implements Codec<S, T> {

    private volatile ObjectMapper objectMapper;

    @Override
    public byte[] toByteArray(S s) {
        try {
            return getObjectMapper().writeValueAsBytes(s);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object", e);
        }
    }

    @Override
    public T fromByteArray(byte[] bytes, Type targetType) {
        try {
            JavaType javaType = getObjectMapper().getTypeFactory().constructType(targetType);
            return getObjectMapper().readValue(bytes, javaType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize object", e);
        }
    }

    @Override
    public int write(OutputStream outputStream, S s) throws IOException {
        getObjectMapper().writeValue(outputStream, s);
        return -1;
    }

    @Override
    public T read(InputStream inputStream, Type targetType) throws IOException {
        try {
            JavaType javaType = getObjectMapper().getTypeFactory().constructType(targetType);
            return getObjectMapper().readValue(inputStream, javaType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize object", e);
        }
    }

    private ObjectMapper getObjectMapper() {
        if (Objects.isNull(this.objectMapper)) {
            synchronized (this) {
                if (Objects.isNull(this.objectMapper)) {
                    this.objectMapper = new ObjectMapper();
                    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                }
            }
        }
        return this.objectMapper;
    }

    static {
        ReflectionUtils.X.forName("com.fasterxml.jackson.databind.ObjectMapper");
    }
}
