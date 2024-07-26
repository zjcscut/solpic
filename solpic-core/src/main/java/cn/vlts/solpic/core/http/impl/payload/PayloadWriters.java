package cn.vlts.solpic.core.http.impl.payload;

import cn.vlts.solpic.core.http.PayloadWriter;

/**
 * Payload writers
 *
 * @author throwable
 * @since 2024/7/26 08:47
 */
public class PayloadWriters {

    public static abstract class BasePayloadWriter implements PayloadWriter {

        protected long contentLength;

        @Override
        public long contentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }
    }

    private static class ByteArrayPayloadWriter extends BasePayloadWriter implements PayloadWriter {

        public void writeContent(byte[] content) {

        }

        @Override
        public Type getType() {
            return Type.BYTES;
        }
    }
}
