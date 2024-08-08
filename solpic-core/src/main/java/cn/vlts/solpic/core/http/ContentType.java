package cn.vlts.solpic.core.http;

import cn.vlts.solpic.core.util.Cursor;
import cn.vlts.solpic.core.util.Pair;
import cn.vlts.solpic.core.util.SimpleLRUCache;
import cn.vlts.solpic.core.util.Tokenizer;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * Content type.
 *
 * @author throwable
 * @since 2024/7/22 23:01
 */
public final class ContentType {

    private static final String CHARSET_KEY = "charset";

    private final static char PARAM_DELIMITER = ';';

    private final static char ELEM_DELIMITER = ',';

    private final static char EQUAL_DELIMITER = '=';

    private static final BitSet TOKEN_DELIMITERS = Tokenizer.newDelimiters(EQUAL_DELIMITER, PARAM_DELIMITER, ELEM_DELIMITER);

    private static final BitSet VALUE_DELIMITERS = Tokenizer.newDelimiters(PARAM_DELIMITER, ELEM_DELIMITER);

    private static final SimpleLRUCache<String, ContentType> CACHE = new SimpleLRUCache<>(16);

    private final String mimeType;

    private final Charset charset;

    private final Pair[] params;

    private String value;

    private ContentType(String mimeType, Charset charset, Pair[] params) {
        this.mimeType = mimeType;
        this.charset = charset;
        this.params = params;
        if (!valid(mimeType)) {
            throw new IllegalArgumentException("Invalid mimeType: " + mimeType);
        }
    }

    public static ContentType parse(String contentType) {
        return parse(contentType, true);
    }

    public static ContentType parse(String contentType, boolean strict) {
        if (Objects.isNull(contentType) || contentType.isEmpty()) {
            return null;
        }
        return CACHE.computeIfAbsent(contentType, ct -> parse0(ct, strict));
    }

    private static ContentType parse0(final CharSequence charSequence, boolean strict) {
        Cursor cursor = new Cursor(charSequence.length());
        Pair mimeTypePair = parsePair(charSequence, cursor);
        if (!cursor.isEnd() && Objects.isNull(mimeTypePair.value())) {
            char ch = charSequence.charAt(cursor.pos() - 1);
            if (ch != ELEM_DELIMITER) {
                Tokenizer.INSTANCE.skipWhiteSpace(charSequence, cursor);
                List<Pair> params = new ArrayList<>();
                while (!cursor.isEnd()) {
                    Pair paramPair = parsePair(charSequence, cursor);
                    params.add(paramPair);
                    ch = charSequence.charAt(cursor.pos() - 1);
                    if (ch == ELEM_DELIMITER) {
                        break;
                    }
                }
                return create(mimeTypePair.name(), params.toArray(new Pair[0]), true);
            }
        }
        return create(mimeTypePair.name(), null, false);
    }

    private static Pair parsePair(CharSequence charSequence, Cursor cursor) {
        String name = Tokenizer.INSTANCE.parseToken(charSequence, cursor, TOKEN_DELIMITERS);
        if (cursor.isEnd()) {
            return Pair.of(name, null);
        }
        int delimiter = charSequence.charAt(cursor.pos());
        cursor.updatePos(cursor.pos() + 1);
        if (EQUAL_DELIMITER != delimiter) {
            return Pair.of(name, null);
        }
        String value = Tokenizer.INSTANCE.parseValue(charSequence, cursor, VALUE_DELIMITERS);
        if (!cursor.isEnd()) {
            cursor.updatePos(cursor.pos() + 1);
        }
        return Pair.of(name, value);
    }

    private static ContentType create(String mimeType) {
        return new ContentType(mimeType, null, null);
    }

    private static ContentType create(String mimeType, Charset charset, Pair[] params) {
        return new ContentType(mimeType, charset, params);
    }

    private static ContentType create(String mimeType, Pair[] params, boolean strict) {
        Charset charset = null;
        if (Objects.nonNull(params)) {
            for (Pair pair : params) {
                if (Objects.nonNull(pair.name()) && pair.name().equalsIgnoreCase(CHARSET_KEY)) {
                    try {
                        charset = Charset.forName(pair.value());
                    } catch (UnsupportedCharsetException e) {
                        if (strict) {
                            throw e;
                        }
                    }
                }
            }
        }
        return create(mimeType, charset, params);
    }

    public boolean hasSameMimeType(ContentType other) {
        return Objects.nonNull(other) && Objects.equals(this.mimeType, other.mimeType);
    }

    private static boolean valid(final String s) {
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch == '"' || ch == ',' || ch == ';' || ch == '\'') {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public String getValue() {
        if (Objects.isNull(this.value)) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.mimeType);
            if (Objects.nonNull(this.params)) {
                builder.append("; ");
                for (int i = 0; i < this.params.length; i++) {
                    if (i > 0) {
                        builder.append("; ");
                    }
                    Pair pair = this.params[i];
                    if (Objects.nonNull(pair.value())) {
                        builder.append(pair.name());
                        builder.append("=");
                        builder.append(pair.value());
                    }
                }
            } else if (Objects.nonNull(this.charset)) {
                builder.append("; charset=");
                builder.append(this.charset.name());
            }
            this.value = builder.toString();
        }
        return this.value;
    }

    public static final ContentType APPLICATION_JSON;
    public static final ContentType TEXT_PLAIN;

    static {
        APPLICATION_JSON = ContentType.create("application/json");
        TEXT_PLAIN = ContentType.create("text/plain");
    }
}
