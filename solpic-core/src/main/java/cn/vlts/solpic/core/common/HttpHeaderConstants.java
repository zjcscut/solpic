package cn.vlts.solpic.core.common;

/**
 * HTTP header constants.
 *
 * @author throwable
 * @since 2024/7/22 23:19
 */
public final class HttpHeaderConstants {

    private HttpHeaderConstants() {

    }

    // ##################### CONTENT TYPE VALUE #####################

    public static final String APPLICATION_JSON_VALUE = "application/json";

    public static final String TEXT_PLAIN_VALUE = "text/plain";

    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

    public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

    // ##################### HEADER KEY #####################

    public static final String HTTP_URL_CONNECTION_STATUS_LINE_KEY = "Huc-Statue-Line";

    public static final String ACCEPT_KEY = "Accept";

    public static final String CONTENT_TYPE_KEY = "Content-Type";

    public static final String CONTENT_LENGTH_KEY = "Content-Length";

    public static final String CONTENT_ENCODING_KEY = "Content-Encoding";

    public static final String TRANSFER_ENCODING_KEY = "Transfer-Encoding";

    public static final String COOKIE_KEY = "Cookie";

    public static final String ALLOW_HEADER_KEY = "Allow";

    public static final String CONTENT_DISPOSITION_KEY = "Content-Disposition";

    // ##################### HEADER VALUE #####################

    public static final String TRANSFER_ENCODING_CHUNKED_VALUE = "chunked";

    public static final String CONTENT_ENCODING_GZIP_VALUE = "gzip";
}
