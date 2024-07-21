package cn.vlts.solpic.core.http.impl;

import cn.vlts.solpic.core.http.HttpHeader;

import java.util.Objects;

/**
 * Basic HTTP header.
 *
 * @author throwable
 * @since 2024/7/19 星期五 16:48
 */
public class BasicHttpHeader implements HttpHeader {

    private final String name;

    private final String value;

    private final boolean sensitive;

    public static BasicHttpHeader of(String name, String value) {
        return new BasicHttpHeader(name, value);
    }

    public static BasicHttpHeader of(String name, String value, boolean sensitive) {
        return new BasicHttpHeader(name, value, sensitive);
    }

    private BasicHttpHeader(String name, String value) {
        this(name, value, false);
    }

    private BasicHttpHeader(String name, String value, boolean sensitive) {
        this.name = name;
        this.value = value;
        this.sensitive = sensitive;
    }

    @Override
    public boolean isSensitive() {
        return this.sensitive;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (Objects.isNull(o) || getClass() != o.getClass()) {
            return false;
        }
        BasicHttpHeader that = (BasicHttpHeader) o;
        return sensitive == that.sensitive && Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, sensitive);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.name()).append(": ");
        String value;
        if (Objects.nonNull(value = value())) {
            builder.append(value);
        }
        return builder.toString();
    }
}
