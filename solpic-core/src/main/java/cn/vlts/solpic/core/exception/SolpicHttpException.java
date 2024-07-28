package cn.vlts.solpic.core.exception;

/**
 * HTTP exception.
 *
 * @author throwable
 * @since 2024/7/28 19:55
 */
public class SolpicHttpException extends RuntimeException {

    public SolpicHttpException(String message) {
        super(message);
    }

    public SolpicHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolpicHttpException(Throwable cause) {
        super(cause);
    }
}
