package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.common.HttpHeaderConstants;
import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.util.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Multipart data builder.
 *
 * @author throwable
 * @since 2024/8/11 01:08
 */
class MultipartDataBuilder implements MultipartData.Builder {

    private static final String BOUNDARY_PREFIX_TEXT = "--";

    private static final byte[] BOUNDARY_PREFIX = BOUNDARY_PREFIX_TEXT.getBytes(StandardCharsets.US_ASCII);

    private static final byte[] FIELD_SEP = ": ".getBytes(StandardCharsets.US_ASCII);

    private static final byte[] CR_LF = "\r\n".getBytes(StandardCharsets.US_ASCII);

    private static final String BOUNDARY_KEY = "boundary";

    private final String boundary;

    private final Charset charset;

    private final List<MultipartData.Part> parts = new ArrayList<>();

    MultipartDataBuilder() {
        this(FastUUIDUtils.X.newRandomUUIDWithoutSeq(), StandardCharsets.UTF_8);
    }

    MultipartDataBuilder(String boundary) {
        this(boundary, StandardCharsets.UTF_8);
    }

    MultipartDataBuilder(String boundary, Charset charset) {
        this.boundary = boundary;
        this.charset = charset;
    }

    public MultipartDataBuilder addTextPart(String name, String value) {
        return addTextPart(name, value, ContentType.newInstance(HttpHeaderConstants.TEXT_PLAIN_VALUE, charset));
    }

    public MultipartDataBuilder addTextPart(String name, String value, ContentType contentType) {
        ArgumentUtils.X.notNull("name", name);
        ArgumentUtils.X.notNull("value", value);
        TextPart textPart = new TextPart(name, value);
        textPart.setContentType(contentType);
        parts.add(textPart);
        return this;
    }

    public MultipartDataBuilder addBinaryPart(String name, byte[] value) {
        return addBinaryPart(name, value, ContentType.APPLICATION_OCTET_STREAM);
    }

    public MultipartDataBuilder addBinaryPart(String name, byte[] value, ContentType contentType) {
        ArgumentUtils.X.notNull("value", value);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(value);
        return addBinaryPart(name, inputStream, contentType);
    }

    public MultipartDataBuilder addBinaryPart(String name, InputStream value) {
        return addBinaryPart(name, value, ContentType.APPLICATION_OCTET_STREAM);
    }

    public MultipartDataBuilder addBinaryPart(String name, InputStream value, ContentType contentType) {
        ArgumentUtils.X.notNull("name", name);
        ArgumentUtils.X.notNull("value", value);
        BinaryPart binaryPart = new BinaryPart(name, value);
        binaryPart.setContentType(contentType);
        parts.add(binaryPart);
        return this;
    }

    public MultipartDataBuilder addFilePart(String name, Path path) {
        return addFilePart(name, null, path, ContentType.APPLICATION_OCTET_STREAM);
    }

    public MultipartDataBuilder addFilePart(String name, Path path, ContentType contentType) {
        return addFilePart(name, null, path, contentType);
    }

    public MultipartDataBuilder addFilePart(String name, String filename, Path path, ContentType contentType) {
        ArgumentUtils.X.notNull("name", name);
        ArgumentUtils.X.notNull("path", path);
        FilePart filePart = new FilePart(name, path);
        filename = Optional.ofNullable(filename).orElse(path.toFile().getName());
        filePart.setContentType(contentType);
        filePart.setFileName(filename);
        parts.add(filePart);
        return this;
    }

    private long computeContentLength() {
        return -1;
    }

    @Override
    public MultipartData build() {
        ContentType contentType = ContentType.newInstance(HttpHeaderConstants.MULTIPART_FORM_DATA_VALUE, null,
                new Pair[]{Pair.of(BOUNDARY_KEY, boundary)});
        long contentLength = computeContentLength();
        return new MultipartData(contentType, contentLength, parts);
    }

    private abstract class BasePart implements MultipartData.Part {

        private final String name;

        private String fileName;

        private ContentType contentType;

        private final CaseInsensitiveMap<Cis, String> headers = new CaseInsensitiveMap<>();

        BasePart(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public ContentType getContentType() {
            return contentType;
        }

        public void setContentType(ContentType contentType) {
            ArgumentUtils.X.notNull("contentType", contentType);
            this.contentType = contentType;
        }

        @Override
        public void addHeader(String name, String value) {
            ArgumentUtils.X.notNull("name", name);
            ArgumentUtils.X.notNull("value", value);
            headers.putIfAbsent0(name, value);
        }

        @Override
        public void removeHeader(String name) {
            ArgumentUtils.X.notNull("name", name);
            headers.remove(Cis.of(name));
        }

        @Override
        public List<Pair> getAllHeaders() {
            List<Pair> pairs = new ArrayList<>();
            headers.forEach((k, v) -> pairs.add(Pair.of(k.toString(), v)));
            return pairs;
        }

        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            // write boundary
            outputStream.write(BOUNDARY_PREFIX);
            outputStream.write(boundary.getBytes(charset));
            outputStream.write(CR_LF);
            // write content disposition
            String contentDispositionValue;
            if (Objects.isNull(fileName)) {
                contentDispositionValue = "form-data; name=\"" + name + "\"";
            } else {
                contentDispositionValue = "form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"";
            }
            writeHeader(HttpHeaderConstants.CONTENT_DISPOSITION_KEY, contentDispositionValue, outputStream);
            // write content type
            if (Objects.nonNull(contentType)) {
                writeHeader(HttpHeaderConstants.CONTENT_TYPE_KEY, contentType.toString(), outputStream);
            }
            // write other headers
            for (Map.Entry<Cis, String> entry : headers.entrySet()) {
                writeHeader(entry.getKey().toString(), entry.getValue(), outputStream);
            }
            // write part body
            outputStream.write(CR_LF);
            writePartBodyTo(outputStream);
        }

        private void writeHeader(String name, String value, OutputStream out) throws IOException {
            out.write(name.getBytes(charset));
            out.write(FIELD_SEP);
            out.write(value.getBytes(charset));
            out.write(CR_LF);
        }

        protected abstract void writePartBodyTo(OutputStream outputStream) throws IOException;
    }

    private class TextPart extends BasePart {

        private final String text;

        public TextPart(String name, String text) {
            super(name);
            this.text = text;
        }

        @Override
        protected void writePartBodyTo(OutputStream outputStream) throws IOException {
            outputStream.write(text.getBytes(charset));
        }
    }

    private class BinaryPart extends BasePart {

        private final InputStream inputStream;

        public BinaryPart(String name, InputStream inputStream) {
            super(name);
            this.inputStream = inputStream;
        }

        @Override
        protected void writePartBodyTo(OutputStream outputStream) throws IOException {
            try (BufferedReader reader = IoUtils.X.newBufferedReader(new InputStreamReader(inputStream, charset))) {
                int b;
                while (-1 != (b = reader.read())) {
                    outputStream.write(b);
                }
            }
        }
    }

    private class FilePart extends BasePart {

        private final Path path;

        public FilePart(String name, Path path) {
            super(name);
            this.path = path;
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("File does not exist: " + path);
            }
        }

        @Override
        protected void writePartBodyTo(OutputStream outputStream) throws IOException {
            try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
                int b;
                while (-1 != (b = reader.read())) {
                    outputStream.write(b);
                }
            }
        }
    }
}
