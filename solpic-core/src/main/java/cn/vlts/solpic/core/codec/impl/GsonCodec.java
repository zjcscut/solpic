package cn.vlts.solpic.core.codec.impl;

import cn.vlts.solpic.core.codec.Codec;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Gson codec.
 *
 * @author throwable
 * @since 2024/7/26 星期五 18:50
 */
public class GsonCodec<S, T> implements Codec<S, T> {

    @Override
    public byte[] toByteArray(S s) {
        return new byte[0];
    }

    @Override
    public ByteBuffer toByteBuffer(S s) {
        return null;
    }

    @Override
    public List<ByteBuffer> toByteBuffers(S s) {
        return Collections.emptyList();
    }

    @Override
    public T fromByteArray(byte[] bytes, Type targetType) {
        return null;
    }

    @Override
    public T fromByteBuffer(ByteBuffer buffer, Type targetType) {
        return null;
    }

    @Override
    public T fromByteBuffers(List<ByteBuffer> bufferList, Type targetType) {
        return null;
    }
}
