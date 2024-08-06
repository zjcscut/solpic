package cn.vlts.solpic.core.codec.impl;

import cn.vlts.solpic.core.codec.Codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * None codec.
 *
 * @author throwable
 * @since 2024/8/7 00:33
 */
public class NoneCodec<S, T> implements Codec<S, T> {

    @Override
    public byte[] toByteArray(S s) {
        return new byte[0];
    }

    @Override
    public T fromByteArray(byte[] bytes, Type targetType) {
        return null;
    }

    @Override
    public int write(OutputStream outputStream, S s) throws IOException {
        return 0;
    }

    @Override
    public T read(InputStream inputStream, Type targetType) throws IOException {
        return null;
    }
}
