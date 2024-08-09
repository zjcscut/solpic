package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.ContentType;
import cn.vlts.solpic.core.util.ArgumentUtils;
import cn.vlts.solpic.core.util.HttpCodecUtils;
import cn.vlts.solpic.core.util.Pair;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Url encoded form builder.
 *
 * @author throwable
 * @since 2024/8/9 星期五 14:21
 */
class UrlEncodedFormBuilder implements UrlEncodedForm.Builder {

    private final Charset charset;

    private final List<Pair> pairs = new ArrayList<>();

    UrlEncodedFormBuilder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public UrlEncodedForm.Builder add(String name, String value) {
        ArgumentUtils.X.notNull("name", name);
        ArgumentUtils.X.notNull("value", value);
        pairs.add(Pair.of(HttpCodecUtils.X.encodeValue(name, charset), HttpCodecUtils.X.encodeValue(value, charset)));
        return this;
    }

    @Override
    public UrlEncodedForm.Builder addEncoded(String encodedName, String encodedValue) {
        ArgumentUtils.X.notNull("encodedName", encodedName);
        ArgumentUtils.X.notNull("encodedValue", encodedValue);
        pairs.add(Pair.of(encodedName, encodedValue));
        return this;
    }

    private long computeContentLength() {
        return -1;
    }

    public UrlEncodedForm build() {
        long contentLength = computeContentLength();
        ContentType contentType = ContentType.newInstance("", charset);
        return new UrlEncodedForm(charset, contentType, contentLength, pairs);
    }

}
