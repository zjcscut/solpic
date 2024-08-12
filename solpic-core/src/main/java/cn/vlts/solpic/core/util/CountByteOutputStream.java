package cn.vlts.solpic.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.LongAdder;

/**
 * Count written bytes.
 *
 * @author throwable
 * @since 2024/8/13 00:53
 */
public class CountByteOutputStream extends OutputStream {

    private final LongAdder counter = new LongAdder();

    @Override
    public void write(int b) throws IOException {
        counter.increment();
    }

    public long getTotal() {
        return counter.sum();
    }
}
