package cn.vlts.solpic.core.util;

import java.util.UUID;

/**
 * Fast UUID utils.
 *
 * @author throwable
 * @since 2024/8/1 星期四 10:06
 */
public enum FastUUIDUtils {
    X;

    private static final int LENGTH = 36;

    private static final int LENGTH_WITHOUT_SEQ = 32;

    private static final boolean USE_JDK_UUID_TO_STRING;

    private static final char[] HEX_DIGITS =
            new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static {
        USE_JDK_UUID_TO_STRING = PlatformUtils.X.getMajorVersion() >= 9;
    }

    public String newRandomUUID() {
        return formatUUID(UUID.randomUUID());
    }

    public String newRandomUUIDWithoutSeq() {
        return formatUUIDWithoutSeq(UUID.randomUUID());
    }

    public String formatUUID(final UUID uuid) {
        if (USE_JDK_UUID_TO_STRING) {
            return uuid.toString();
        }
        final long msb = uuid.getMostSignificantBits();
        final long lsb = uuid.getLeastSignificantBits();
        final char[] uuidChars = new char[LENGTH];
        uuidChars[0] = HEX_DIGITS[(int) ((msb & 0xf000000000000000L) >>> 60)];
        uuidChars[1] = HEX_DIGITS[(int) ((msb & 0x0f00000000000000L) >>> 56)];
        uuidChars[2] = HEX_DIGITS[(int) ((msb & 0x00f0000000000000L) >>> 52)];
        uuidChars[3] = HEX_DIGITS[(int) ((msb & 0x000f000000000000L) >>> 48)];
        uuidChars[4] = HEX_DIGITS[(int) ((msb & 0x0000f00000000000L) >>> 44)];
        uuidChars[5] = HEX_DIGITS[(int) ((msb & 0x00000f0000000000L) >>> 40)];
        uuidChars[6] = HEX_DIGITS[(int) ((msb & 0x000000f000000000L) >>> 36)];
        uuidChars[7] = HEX_DIGITS[(int) ((msb & 0x0000000f00000000L) >>> 32)];
        uuidChars[8] = '-';
        uuidChars[9] = HEX_DIGITS[(int) ((msb & 0x00000000f0000000L) >>> 28)];
        uuidChars[10] = HEX_DIGITS[(int) ((msb & 0x000000000f000000L) >>> 24)];
        uuidChars[11] = HEX_DIGITS[(int) ((msb & 0x0000000000f00000L) >>> 20)];
        uuidChars[12] = HEX_DIGITS[(int) ((msb & 0x00000000000f0000L) >>> 16)];
        uuidChars[13] = '-';
        uuidChars[14] = HEX_DIGITS[(int) ((msb & 0x000000000000f000L) >>> 12)];
        uuidChars[15] = HEX_DIGITS[(int) ((msb & 0x0000000000000f00L) >>> 8)];
        uuidChars[16] = HEX_DIGITS[(int) ((msb & 0x00000000000000f0L) >>> 4)];
        uuidChars[17] = HEX_DIGITS[(int) (msb & 0x000000000000000fL)];
        uuidChars[18] = '-';
        uuidChars[19] = HEX_DIGITS[(int) ((lsb & 0xf000000000000000L) >>> 60)];
        uuidChars[20] = HEX_DIGITS[(int) ((lsb & 0x0f00000000000000L) >>> 56)];
        uuidChars[21] = HEX_DIGITS[(int) ((lsb & 0x00f0000000000000L) >>> 52)];
        uuidChars[22] = HEX_DIGITS[(int) ((lsb & 0x000f000000000000L) >>> 48)];
        uuidChars[23] = '-';
        uuidChars[24] = HEX_DIGITS[(int) ((lsb & 0x0000f00000000000L) >>> 44)];
        uuidChars[25] = HEX_DIGITS[(int) ((lsb & 0x00000f0000000000L) >>> 40)];
        uuidChars[26] = HEX_DIGITS[(int) ((lsb & 0x000000f000000000L) >>> 36)];
        uuidChars[27] = HEX_DIGITS[(int) ((lsb & 0x0000000f00000000L) >>> 32)];
        uuidChars[28] = HEX_DIGITS[(int) ((lsb & 0x00000000f0000000L) >>> 28)];
        uuidChars[29] = HEX_DIGITS[(int) ((lsb & 0x000000000f000000L) >>> 24)];
        uuidChars[30] = HEX_DIGITS[(int) ((lsb & 0x0000000000f00000L) >>> 20)];
        uuidChars[31] = HEX_DIGITS[(int) ((lsb & 0x00000000000f0000L) >>> 16)];
        uuidChars[32] = HEX_DIGITS[(int) ((lsb & 0x000000000000f000L) >>> 12)];
        uuidChars[33] = HEX_DIGITS[(int) ((lsb & 0x0000000000000f00L) >>> 8)];
        uuidChars[34] = HEX_DIGITS[(int) ((lsb & 0x00000000000000f0L) >>> 4)];
        uuidChars[35] = HEX_DIGITS[(int) (lsb & 0x000000000000000fL)];
        return new String(uuidChars);
    }

    public String formatUUIDWithoutSeq(final UUID uuid) {
        if (USE_JDK_UUID_TO_STRING) {
            return uuid.toString();
        }
        final long msb = uuid.getMostSignificantBits();
        final long lsb = uuid.getLeastSignificantBits();
        final char[] uuidChars = new char[LENGTH_WITHOUT_SEQ];
        uuidChars[0] = HEX_DIGITS[(int) ((msb & 0xf000000000000000L) >>> 60)];
        uuidChars[1] = HEX_DIGITS[(int) ((msb & 0x0f00000000000000L) >>> 56)];
        uuidChars[2] = HEX_DIGITS[(int) ((msb & 0x00f0000000000000L) >>> 52)];
        uuidChars[3] = HEX_DIGITS[(int) ((msb & 0x000f000000000000L) >>> 48)];
        uuidChars[4] = HEX_DIGITS[(int) ((msb & 0x0000f00000000000L) >>> 44)];
        uuidChars[5] = HEX_DIGITS[(int) ((msb & 0x00000f0000000000L) >>> 40)];
        uuidChars[6] = HEX_DIGITS[(int) ((msb & 0x000000f000000000L) >>> 36)];
        uuidChars[7] = HEX_DIGITS[(int) ((msb & 0x0000000f00000000L) >>> 32)];
        uuidChars[8] = HEX_DIGITS[(int) ((msb & 0x00000000f0000000L) >>> 28)];
        uuidChars[9] = HEX_DIGITS[(int) ((msb & 0x000000000f000000L) >>> 24)];
        uuidChars[10] = HEX_DIGITS[(int) ((msb & 0x0000000000f00000L) >>> 20)];
        uuidChars[11] = HEX_DIGITS[(int) ((msb & 0x00000000000f0000L) >>> 16)];
        uuidChars[12] = HEX_DIGITS[(int) ((msb & 0x000000000000f000L) >>> 12)];
        uuidChars[13] = HEX_DIGITS[(int) ((msb & 0x0000000000000f00L) >>> 8)];
        uuidChars[14] = HEX_DIGITS[(int) ((msb & 0x00000000000000f0L) >>> 4)];
        uuidChars[15] = HEX_DIGITS[(int) (msb & 0x000000000000000fL)];
        uuidChars[16] = HEX_DIGITS[(int) ((lsb & 0xf000000000000000L) >>> 60)];
        uuidChars[17] = HEX_DIGITS[(int) ((lsb & 0x0f00000000000000L) >>> 56)];
        uuidChars[18] = HEX_DIGITS[(int) ((lsb & 0x00f0000000000000L) >>> 52)];
        uuidChars[19] = HEX_DIGITS[(int) ((lsb & 0x000f000000000000L) >>> 48)];
        uuidChars[20] = HEX_DIGITS[(int) ((lsb & 0x0000f00000000000L) >>> 44)];
        uuidChars[21] = HEX_DIGITS[(int) ((lsb & 0x00000f0000000000L) >>> 40)];
        uuidChars[22] = HEX_DIGITS[(int) ((lsb & 0x000000f000000000L) >>> 36)];
        uuidChars[23] = HEX_DIGITS[(int) ((lsb & 0x0000000f00000000L) >>> 32)];
        uuidChars[24] = HEX_DIGITS[(int) ((lsb & 0x00000000f0000000L) >>> 28)];
        uuidChars[25] = HEX_DIGITS[(int) ((lsb & 0x000000000f000000L) >>> 24)];
        uuidChars[26] = HEX_DIGITS[(int) ((lsb & 0x0000000000f00000L) >>> 20)];
        uuidChars[27] = HEX_DIGITS[(int) ((lsb & 0x00000000000f0000L) >>> 16)];
        uuidChars[28] = HEX_DIGITS[(int) ((lsb & 0x000000000000f000L) >>> 12)];
        uuidChars[29] = HEX_DIGITS[(int) ((lsb & 0x0000000000000f00L) >>> 8)];
        uuidChars[30] = HEX_DIGITS[(int) ((lsb & 0x00000000000000f0L) >>> 4)];
        uuidChars[31] = HEX_DIGITS[(int) (lsb & 0x000000000000000fL)];
        return new String(uuidChars);
    }
}
