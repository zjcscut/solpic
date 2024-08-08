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
@SuppressWarnings({"unchecked", "rawtypes"})
public enum CodecFactory {
    X;

    private volatile SpiLoader<Codec> spiLoader;

    private final ConcurrentMap<CodecType, Codec> cache = new ConcurrentHashMap<>();

    public <S, T> Codec<S, T> loadCodec(CodecType codecType, String codecName) {
        if (Objects.nonNull(codecName)) {
            return (Codec<S, T>) getSpiLoader().getService(codecName);
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
        Codec codec = getSpiLoader().getAvailableServices().stream().findFirst().orElse(null);
        if (Objects.isNull(codec)) {
            codec = CodecType.getAvailableCodecTypes().stream().findFirst()
                    .map(codecType -> cache.computeIfAbsent(codecType,
                            k -> (Codec) ReflectionUtils.X.createInstance(ReflectionUtils.X.forName(k.getType()))))
                    .orElse(null);
        }
        return (Codec<S, T>) codec;
    }

    private SpiLoader<Codec> getSpiLoader() {
        if (Objects.isNull(spiLoader)) {
            synchronized (this) {
                spiLoader = SpiLoader.getSpiLoader(Codec.class);
            }
        }
        return spiLoader;
    }
}
