package cn.vlts.solpic.core.codec;

import cn.vlts.solpic.core.spi.SpiLoader;
import cn.vlts.solpic.core.util.ReflectionUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Codec factory.
 *
 * @author throwable
 * @since 2024/7/26 星期五 18:43
 */
@SuppressWarnings("unchecked")
public enum CodecFactory {
    X;

    private final SpiLoader<Codec> spiLoader = SpiLoader.getSpiLoader(Codec.class);

    private final ConcurrentMap<CodecType,Codec> cache = new ConcurrentHashMap<>();

    public <S, T> Codec<S, T> loadCodec(CodecType codecType, String codecName) {
        if (Objects.nonNull(codecName)) {
            return (Codec<S, T>) spiLoader.getService(codecName);
        }
        if (Objects.nonNull(codecType) && CodecType.checkAvailableCodecType(codecType)) {
            return (Codec<S, T>)
                    cache.computeIfAbsent(codecType,
                            k -> (Codec) ReflectionUtils.X.createInstance(ReflectionUtils.X.forName(k.getType())));
        }
        Codec<S, T> matchedCodec = loadBestMatchedCodec();
        if (Objects.nonNull(matchedCodec)) {
            return matchedCodec;
        }
        throw new IllegalStateException("Failed to load codec");
    }

    public <S, T> Codec<S, T> loadBestMatchedCodec() {
        Codec codec = spiLoader.getAvailableServices().stream().findFirst().orElse(null);
        if (Objects.isNull(codec)) {
            codec = CodecType.getAvailableCodecTypes().stream().findFirst()
                    .map(codecType -> ReflectionUtils.X.forName(codecType.getType()))
                    .map(clazz -> (Codec) ReflectionUtils.X.createInstance(clazz))
                    .orElse(null);
        }
        return (Codec<S, T>) codec;
    }
}
