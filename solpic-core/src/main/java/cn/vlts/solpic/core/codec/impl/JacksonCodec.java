package cn.vlts.solpic.core.codec.impl;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.util.ReflectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Jackson codec.
 *
 * @author throwable
 * @since 2024/7/27 15:44
 */
public class JacksonCodec<S, T> implements Codec<S, T> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] toByteArray(S s) {
        try {
            return objectMapper.writeValueAsBytes(s);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize object", e);
        }
    }

    @Override
    public T fromByteArray(byte[] bytes, Type targetType) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructType(targetType);
            return objectMapper.readValue(bytes, javaType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize object", e);
        }
    }

    @Override
    public int write(OutputStream outputStream, S s) throws IOException {
        objectMapper.writeValue(outputStream, s);
        return -1;
    }

    @Override
    public T read(InputStream inputStream, Type targetType) throws IOException {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructType(targetType);
            return objectMapper.readValue(inputStream, javaType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize object", e);
        }
    }

    static {
        ReflectionUtils.X.forName("com.fasterxml.jackson.databind.ObjectMapper");
    }
}
