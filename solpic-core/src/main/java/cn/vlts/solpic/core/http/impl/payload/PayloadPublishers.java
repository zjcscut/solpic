package cn.vlts.solpic.core.http.impl.payload;

import cn.vlts.solpic.core.http.PayloadPublisher;
import cn.vlts.solpic.core.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Payload publisher impls.
 *
 * @author throwable
 * @since 2024/7/26 星期五 10:45
 */
public final class PayloadPublishers {

    public static final DefaultPayloadPublishers DEFAULT;

    public static final StreamPayloadPublishers STREAM;

    public static class DefaultPayloadPublishers {

        public static PayloadPublisher ofString(String content) {
            return ofString(content, StandardCharsets.UTF_8);
        }

        public static PayloadPublisher ofString(String content, Charset charset) {
            return ofByteArray(content.getBytes(charset));
        }

        public static PayloadPublisher ofByteArray(byte[] bytes) {
            return new DefaultPayloadPublisher(bytes, bytes.length);
        }

        public static PayloadPublisher ofByteArray(byte[] bytes, int offset, int length) {
            int sl = bytes.length;
            if (offset == 0 && sl == length) {
                return new DefaultPayloadPublisher(bytes, bytes.length);
            }
            byte[] dest = new byte[length];
            System.arraycopy(bytes, offset, dest, 0, length);
            return new DefaultPayloadPublisher(dest, dest.length);
        }

        public static PayloadPublisher ofInputStream(InputStream in) {
            return ofInputStream(in, -1);
        }

        public static PayloadPublisher ofInputStream(InputStream in, long length) {
            byte[] bytes = IoUtils.X.readBytes(in, true);
            long l = length != -1 ? length : bytes.length;
            return new DefaultPayloadPublisher(IoUtils.X.readBytes(in, true), l);
        }

        public static PayloadPublisher ofFile(Path path) {
            long length;
            try {
                length = Files.size(path);
            } catch (IOException ignore) {
                length = -1;
            }
            InputStream inputStream;
            try {
                inputStream = Files.newInputStream(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return ofInputStream(inputStream, length);
        }
    }

    public static class StreamPayloadPublishers {

    }

    static {
        DEFAULT = new DefaultPayloadPublishers();
        STREAM = new StreamPayloadPublishers();
    }
}
