package cn.vlts.solpic.core.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * HTTP protocol version.
 *
 * @author throwable
 * @since 2024/7/19 星期五 17:37
 */
@RequiredArgsConstructor
@Getter
public enum HttpVersion {

    HTTP_0_9("HTTP", 0, 9),

    HTTP_1("HTTP", 1, 0),

    HTTP_1_1("HTTP", 1, 1),

    HTTP_2("HTTP", 2, 0),

    ;

    private final String protocol;

    private final int major;

    private final int minor;

    public static HttpVersion defaultVersion() {
        return HTTP_1_1;
    }

    public boolean isSameAs(HttpVersion otherVersion) {
        return Objects.nonNull(otherVersion) && this.major == otherVersion.major && this.minor == otherVersion.minor;
    }

    public String getValue() {
        return String.format("%s/%d.%d", protocol, major, minor);
    }

    public String getVersion() {
        return String.format("%d.%d", major, minor);
    }

    public static HttpVersion fromVersion(String versionValue) {
        for (HttpVersion httpVersion : values()) {
            if (Objects.equals(versionValue, httpVersion.getVersion())) {
                return httpVersion;
            }
        }
        return null;
    }

    public static HttpVersion fromVersion(int major, int minor) {
        for (HttpVersion httpVersion : HttpVersion.values()) {
            if (httpVersion.getMajor() == major && httpVersion.getMinor() == major) {
                return httpVersion;
            }
        }
        return null;
    }

    public static HttpVersion fromStatusLine(String statusLine) {
        HttpVersion httpVersion = defaultVersion();
        if (Objects.nonNull(statusLine) && statusLine.startsWith("HTTP/")) {
            int protocolStartPos = 5;
            int protocolEndPos = statusLine.indexOf(' ', protocolStartPos + 1);
            if (protocolEndPos == -1) {
                protocolEndPos = statusLine.indexOf('\t', protocolStartPos + 1);
            }
            if (protocolEndPos > protocolStartPos && protocolEndPos < statusLine.length()) {
                try {
                    int major, minor;
                    String version = statusLine.substring(protocolStartPos, protocolEndPos);
                    String[] strings = version.split("\\.");
                    if (strings.length == 1) {
                        major = Integer.parseInt(strings[0]);
                        HttpVersion v = fromVersion(major, 0);
                        if (Objects.nonNull(v)) {
                            httpVersion = v;
                        }
                    } else if (strings.length == 2) {
                        major = Integer.parseInt(strings[0]);
                        minor = Integer.parseInt(strings[1]);
                        HttpVersion v = fromVersion(major, minor);
                        if (Objects.nonNull(v)) {
                            httpVersion = v;
                        }
                    }
                } catch (Throwable ignore) {

                }
            }
        }
        return httpVersion;
    }
}
