package cn.vlts.solpic.core.common;

/**
 * Default HTTP status code.
 *
 * @author throwable
 * @since 2024/7/23 23:30
 */
public class DefaultHttpStatusCode implements HttpStatusCode {

    private final int value;

    private final HttpStatusSeries series;

    public DefaultHttpStatusCode(int value) {
        this.value = value;
        this.series = HttpStatusSeries.fromStatusCode(value);
    }

    @Override
    public int value() {
        return this.value;
    }

    @Override
    public boolean isInformational() {
        return this.series == HttpStatusSeries.INFORMATIONAL;
    }

    @Override
    public boolean isSuccessful() {
        return this.series == HttpStatusSeries.SUCCESSFUL;
    }

    @Override
    public boolean isRedirection() {
        return this.series == HttpStatusSeries.REDIRECTION;
    }

    @Override
    public boolean isClientError() {
        return this.series == HttpStatusSeries.CLIENT_ERROR;
    }

    @Override
    public boolean isServerError() {
        return this.series == HttpStatusSeries.SERVER_ERROR;
    }

    @Override
    public boolean isError() {
        return isClientError() || isServerError();
    }
}
