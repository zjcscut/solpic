package cn.vlts.solpic.core.codec.impl;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.util.ReflectionUtils;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

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
}
