package cn.vlts.solpic.core.util;

import lombok.Data;

/**
 * Box.
 *
 * @author throwable
 * @since 2024/7/21 19:14
 */
@Data
public class Box<T> {

    private volatile T value;

    public Box(T value) {
        this.value = value;
    }

    public Box() {
    }
}
