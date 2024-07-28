package cn.vlts.solpic.core.codec.impl;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.codec.CodecFactory;
import cn.vlts.solpic.core.util.ReflectionUtils;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gson codec.
 *
 * @author throwable
 * @since 2024/7/26 星期五 18:50
 */
public class GsonCodec<S, T> implements Codec<S, T> {

    private final Gson gson = new Gson();

    @Override
    public byte[] toByteArray(S s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bos, StandardCharsets.UTF_8))) {
            gson.toJson(s, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bos.toByteArray();
    }

    @Override
    public T fromByteArray(byte[] bytes, Type targetType) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, targetType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int write(OutputStream outputStream, S s) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        gson.toJson(s, writer);
        return -1;
    }

    @Override
    public T read(InputStream inputStream, Type targetType) throws IOException {
        return gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), targetType);
    }

    static {
        ReflectionUtils.X.forName("com.google.gson.Gson");
    }

    @RequiredArgsConstructor
    @Getter
    @ToString
    public static class User {
        private final Long id;
        private final String name;
    }

    public static void main(String[] args) {
        Codec codec = CodecFactory.X.loadCodec(null, null);
        User user = new User(1L, "doge");
        byte[] byteArray = codec.toByteArray(user);
        System.out.println(byteArray.length);
        Object o = codec.fromByteArray(byteArray, User.class);
        System.out.println(o);
        ByteBuffer byteBuffer = codec.toByteBuffer(o);
        o = codec.fromByteBuffer(byteBuffer, User.class);
        System.out.println(o);
        List byteBuffers = codec.toByteBuffers(o);
        o = codec.fromByteBuffers(byteBuffers, User.class);
        System.out.println(o);
    }
}
