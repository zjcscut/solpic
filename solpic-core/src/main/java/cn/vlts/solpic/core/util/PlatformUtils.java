package cn.vlts.solpic.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Platform utils.
 *
 * @author throwable
 * @since 2024/8/1 星期四 10:18
 */
public enum PlatformUtils {
    X;

    private static JavaVersion VERSION;

    static {
        try {
            String sv = System.getProperty("java.specification.version");
            String[] svs = sv.split("\\.");
            VERSION = new JavaVersion(Integer.parseInt(svs[0]), svs.length > 1 ? Integer.parseInt(svs[1]) : 0,
                    svs.length > 2 ? Integer.parseInt(svs[2]) : 0, false);
        } catch (Throwable ignore) {
            VERSION = new JavaVersion(0, 0, 0, true);
        }
    }

    public int getMajorVersion() {
        return VERSION.getMajor();
    }

    public int getMinorVersion() {
        return VERSION.getMinor();
    }

    public int getUpdateVersion() {
        return VERSION.getUpdate();
    }

    @RequiredArgsConstructor
    @Getter
    public static class JavaVersion {

        private final int major;

        private final int minor;

        private final int update;

        private final boolean error;
    }
}
