package cn.vlts.solpic.core.exception;

/**
 * HTTP exception.
 *
 * @author throwable
 * @since 2024/7/28 19:55
 */
public class SolpicHttpException extends RuntimeException {

    private final boolean aborted;

    public SolpicHttpException(String message, boolean aborted) {
        super(message);
        this.aborted = aborted;
    }

    public SolpicHttpException(String message) {
        super(message);
        this.aborted = false;
    }

    public SolpicHttpException(String message, Throwable cause) {
        super(message, cause);
        this.aborted = false;
    }

    public SolpicHttpException(Throwable cause) {
        super(cause);
        this.aborted = false;
    }

    public boolean isAborted() {
        return aborted;
    }
}
