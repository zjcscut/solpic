package cn.vlts.solpic.core.codec.impl;

import cn.vlts.solpic.core.codec.Codec;
import cn.vlts.solpic.core.util.ReflectionUtils;
import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Fastjson codec.
 *
 * @author throwable
 * @since 2024/7/27 15:45
 */
public class FastJsonCodec<S, T> implements Codec<S, T> {

    @Override
    public byte[] toByteArray(S s) {
        return JSON.toJSONBytes(s);
    }

    @Override
    public T fromByteArray(byte[] bytes, Type targetType) {
        return JSON.parseObject(bytes, targetType);
    }

    @Override
    public int write(OutputStream outputStream, S s) throws IOException {
        return JSON.writeJSONString(outputStream, s);
    }

    @Override
    public T read(InputStream inputStream, Type targetType) throws IOException {
        return JSON.parseObject(inputStream, targetType);
    }

    static {
        ReflectionUtils.X.forName("com.alibaba.fastjson.JSON");
    }
}
