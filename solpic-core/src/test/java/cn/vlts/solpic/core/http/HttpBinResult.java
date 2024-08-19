package cn.vlts.solpic.core.http;

import lombok.Data;

import java.util.Map;

/**
 * HttpBin result.
 *
 * @author throwable
 * @since 2024/8/19 星期一 9:36
 */
@Data
public class HttpBinResult {

    private Map<String, String> args;
    private Map<String, String> headers;
    private String origin;
    private String url;
}
