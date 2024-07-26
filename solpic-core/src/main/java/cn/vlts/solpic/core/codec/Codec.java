package cn.vlts.solpic.core.codec;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Codec.
 *
 * @author throwable
 * @since 2024/7/26 星期五 14:36
 */
public interface Codec<S, T> {

    byte[] toByteArray(S s);

    ByteBuffer toByteBuffer(S s);

    List<ByteBuffer> toByteBuffers(S s);

    T fromByteArray(byte[] bytes, Type targetType);

    T fromByteBuffer(ByteBuffer buffer, Type targetType);

    T fromByteBuffers(List<ByteBuffer> bufferList, Type targetType);
}
