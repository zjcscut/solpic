package cn.vlts.solpic.core.codec;

/**
 * Codec factory.
 *
 * @author throwable
 * @since 2024/7/26 星期五 18:43
 */
public enum CodecFactory {
    X;

    public <S, T> Codec<S, T> loadCodec(CodecType codecType) {
        return null;
    }

    public <S, T> Codec<S, T> loadBestMatchedCodec() {
        return null;
    }
}
