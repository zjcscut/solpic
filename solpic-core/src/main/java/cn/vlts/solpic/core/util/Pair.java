package cn.vlts.solpic.core.util;

/**
 * Pair for name and value.
 *
 * @author throwable
 * @since 2024/7/19 星期五 16:03
 */
public interface Pair {

    String name();

    String value();

    static Pair of(String name, String value) {
        return new BasicPair(name, value);
    }

    class BasicPair implements Pair {

        private final String name;

        private final String value;

        BasicPair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
