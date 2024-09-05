package cn.vlts.solpic.core.exception;

import cn.vlts.solpic.core.http.HttpRequest;

/**
 * HTTP exception.
 *
 * @author throwable
 * @since 2024/7/28 19:55
 */
public class SolpicHttpException extends RuntimeException {

    private final boolean aborted;

    private final HttpRequest request;

    public SolpicHttpException(String message, HttpRequest httpRequest, boolean aborted) {
        super(message);
        this.request = httpRequest;
        this.aborted = aborted;
    }

    public SolpicHttpException(String message, HttpRequest httpRequest) {
        super(message);
        this.request = httpRequest;
        this.aborted = false;
    }

    public SolpicHttpException(String message, Throwable cause, HttpRequest httpRequest) {
        super(message, cause);
        this.aborted = false;
        this.request = httpRequest;
    }

    public SolpicHttpException(Throwable cause, HttpRequest httpRequest) {
        super(cause);
        this.aborted = false;
        this.request = httpRequest;
    }

    public boolean isAborted() {
        return aborted;
    }

    public HttpRequest getRequest() {
        return request;
    }
}
