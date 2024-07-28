package cn.vlts.solpic.core.codec;

import cn.vlts.solpic.core.util.ReflectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Codec type.
 *
 * @author throwable
 * @since 2024/7/26 星期五 18:45
 */
@RequiredArgsConstructor
@Getter
public enum CodecType {

    JACKSON("cn.vlts.solpic.core.codec.impl.JacksonCodec", 1),

    GSON("cn.vlts.solpic.core.codec.impl.GsonCodec", 4),

    FASTJSON("cn.vlts.solpic.core.codec.impl.FastJsonCodec", 3),

    FASTJSON2("cn.vlts.solpic.core.codec.impl.FastJson2Codec", 2),

    ;

    private static final List<CodecType> AVAILABLE_CODEC_TYPES = new ArrayList<>();

    static {
        Stream.of(CodecType.values()).forEach(ct -> {
            if (ReflectionUtils.X.isClassPresent(ct.getType())) {
                AVAILABLE_CODEC_TYPES.add(ct);
            }
        });
        AVAILABLE_CODEC_TYPES.sort(Comparator.comparing(CodecType::getOrder));
    }

    private final String type;

    private final int order;

    public static List<CodecType> getAvailableCodecTypes() {
        return Collections.unmodifiableList(AVAILABLE_CODEC_TYPES);
    }

    public static boolean checkAvailableCodecType(CodecType codecType) {
        return AVAILABLE_CODEC_TYPES.contains(codecType);
    }
}
