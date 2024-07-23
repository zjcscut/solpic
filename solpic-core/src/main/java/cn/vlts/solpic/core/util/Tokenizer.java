package cn.vlts.solpic.core.util;

import java.util.BitSet;
import java.util.Objects;

/**
 * Tokenizer. Reference to apache http client5 Tokenizer.
 *
 * @author throwable
 * @since 2024/7/23 星期二 11:50
 */
public final class Tokenizer {

    public static final Tokenizer INSTANCE = new Tokenizer();

    public static final char DOUBLE_QUOTE = '\"';

    public static final char SINGLE_QUOTE = '\'';

    public static final char ESCAPE = '\\';

    public static final char SPACE = ' ';

    public static final int CR = 13;

    public static final int LF = 10;

    public static final int SP = 32;

    public static final int HT = 9;

    public static boolean isWhitespace(final char ch) {
        return ch == SP || ch == HT || ch == CR || ch == LF;
    }

    public static BitSet newDelimiters(int... bits) {
        BitSet bitSet = new BitSet();
        for (int b : bits) {
            bitSet.set(b);
        }
        return bitSet;
    }

    public String parseContent(CharSequence buf, Cursor cursor, BitSet delimiters) {
        StringBuilder dist = new StringBuilder();
        copyContent(buf, cursor, delimiters, dist, (pos, c, d) -> (Objects.nonNull(d) && d.get(c)) || isWhitespace(c));
        return dist.toString();
    }

    public void copyContent(CharSequence buf, Cursor cursor, BitSet delimiters, StringBuilder dist) {
        copyContent(buf, cursor, delimiters, dist, (pos, c, d) -> (Objects.nonNull(d) && d.get(c)) || isWhitespace(c));
    }

    public void copyContent(CharSequence buf,
                            Cursor cursor,
                            BitSet delimiters,
                            StringBuilder dist,
                            TriPredicate<Integer, Character, BitSet> breakPredicate) {
        int pos = cursor.pos();
        int to = cursor.upper();
        for (int i = pos; i < to; i++) {
            char current = buf.charAt(i);
            if (breakPredicate.test(i, current, delimiters)) {
                break;
            }
            pos++;
            dist.append(current);
        }
        cursor.updatePos(pos);
    }

    public String copyUnquotedContent(CharSequence buf, Cursor cursor, BitSet delimiters) {
        StringBuilder dist = new StringBuilder();
        copyContent(buf, cursor, delimiters, dist, (pos, c, d) -> (Objects.nonNull(d) && d.get(c)) || isWhitespace(c)
                || c == DOUBLE_QUOTE || c == SINGLE_QUOTE);
        return dist.toString();
    }

    public void copyUnquotedContent(CharSequence buf, Cursor cursor, BitSet delimiters, StringBuilder dist) {
        copyContent(buf, cursor, delimiters, dist, (pos, c, d) -> (Objects.nonNull(d) && d.get(c)) || isWhitespace(c)
                || c == DOUBLE_QUOTE || c == SINGLE_QUOTE);
    }

    public String copyQuotedContent(CharSequence buf, Cursor cursor) {
        StringBuilder dist = new StringBuilder();
        copyQuotedContent(buf, cursor, dist);
        return dist.toString();
    }

    public void copyQuotedContent(CharSequence buf, Cursor cursor, StringBuilder dist) {
        if (cursor.isEnd()) {
            return;
        }
        int pos = cursor.pos();
        int to = cursor.upper();
        char current = buf.charAt(pos);
        if (current != DOUBLE_QUOTE && current != SINGLE_QUOTE) {
            return;
        }
        pos++;
        boolean escaped = false;
        for (int i = pos; i < to; i++) {
            current = buf.charAt(pos);
            if (escaped) {
                if (current != DOUBLE_QUOTE && current != ESCAPE && current != SINGLE_QUOTE) {
                    dist.append(ESCAPE);
                }
                dist.append(current);
                escaped = false;
            } else {
                if (current == DOUBLE_QUOTE || current == SINGLE_QUOTE) {
                    pos++;
                    break;
                }
                if (current == ESCAPE) {
                    escaped = true;
                } else if (current != CR && current != LF) {
                    dist.append(current);
                }
            }
        }
        cursor.updatePos(pos);
    }

    public String parseToken(CharSequence buf, Cursor cursor, BitSet delimiters) {
        return parseToken(buf, cursor, delimiters, (pos, c, d) -> Objects.nonNull(d) && d.get(c));
    }

    public String parseToken(CharSequence buf, Cursor cursor, BitSet delimiters,
                             TriPredicate<Integer, Character, BitSet> breakPredicate) {
        StringBuilder dist = new StringBuilder();
        boolean skipWhiteSpace = false;
        while (!cursor.isEnd()) {
            int pos = cursor.pos();
            char current = buf.charAt(pos);
            if (breakPredicate.test(pos, current, delimiters)) {
                break;
            } else if (isWhitespace(current)) {
                skipWhiteSpace(buf, cursor);
                skipWhiteSpace = true;
            } else {
                if (skipWhiteSpace && dist.length() > 0) {
                    dist.append(SPACE);
                }
                copyContent(buf, cursor, delimiters, dist);
                skipWhiteSpace = false;
            }
        }
        return dist.toString();
    }

    public String parseValue(CharSequence buf, Cursor cursor, BitSet delimiters) {
        return parseValue(buf, cursor, delimiters, (pos, c, d) -> Objects.nonNull(d) && d.get(c));
    }

    public String parseValue(CharSequence buf, Cursor cursor, BitSet delimiters,
                             TriPredicate<Integer, Character, BitSet> breakPredicate) {
        StringBuilder dist = new StringBuilder();
        boolean skipWhiteSpace = false;
        while (!cursor.isEnd()) {
            int pos = cursor.pos();
            char current = buf.charAt(pos);
            if (breakPredicate.test(pos, current, delimiters)) {
                break;
            } else if (isWhitespace(current)) {
                skipWhiteSpace(buf, cursor);
                skipWhiteSpace = true;
            } else if (current == DOUBLE_QUOTE || current == SINGLE_QUOTE) {
                if (skipWhiteSpace && dist.length() > 0) {
                    dist.append(SPACE);
                }
                copyQuotedContent(buf, cursor, dist);
                skipWhiteSpace = false;
            } else {
                if (skipWhiteSpace && dist.length() > 0) {
                    dist.append(SPACE);
                }
                copyUnquotedContent(buf, cursor, delimiters, dist);
                skipWhiteSpace = false;
            }
        }
        return dist.toString();
    }

    public void skipWhiteSpace(CharSequence buf, Cursor cursor) {
        int pos = cursor.pos();
        int to = cursor.upper();
        for (int i = pos; i < to; i++) {
            char current = buf.charAt(i);
            if (!isWhitespace(current)) {
                break;
            }
            pos++;
        }
        cursor.updatePos(pos);
    }
}
