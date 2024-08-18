package cn.vlts.solpic.core.http.bind;

import cn.vlts.solpic.core.http.HttpHeader;
import cn.vlts.solpic.core.http.HttpRequest;
import cn.vlts.solpic.core.http.RequestPayloadSupport;
import cn.vlts.solpic.core.util.HttpCodecUtils;
import cn.vlts.solpic.core.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Request parameter handler.
 *
 * @author throwable
 * @since 2024/8/17 15:33
 */
@FunctionalInterface
@SuppressWarnings("unchecked")
public interface RequestParameterHandler<T> {

    void apply(Supplier<T> supplier, HttpRequest.Builder builder);

    class Header implements RequestParameterHandler<String> {

        private final String headerName;

        public Header(String headerName) {
            this.headerName = headerName;
        }

        @Override
        public void apply(Supplier<String> supplier, HttpRequest.Builder builder) {
            String headerValue = supplier.get();
            if (Objects.nonNull(headerValue)) {
                builder.header(headerName, headerValue);
            }
        }
    }

    class Headers implements RequestParameterHandler<Iterable<HttpHeader>> {

        @Override
        public void apply(Supplier<Iterable<HttpHeader>> supplier, HttpRequest.Builder builder) {
            Iterable<HttpHeader> httpHeaders = supplier.get();
            if (Objects.nonNull(httpHeaders)) {
                httpHeaders.forEach(builder::header);
            }
        }
    }

    class HeaderMap implements RequestParameterHandler<Map> {

        @Override
        public void apply(Supplier<Map> supplier, HttpRequest.Builder builder) {
            Map headerMap = supplier.get();
            if (Objects.nonNull(headerMap)) {
                headerMap.forEach((k, v) -> builder.header(String.valueOf(k), String.valueOf(v)));
            }
        }
    }

    class Form implements RequestParameterHandler<UrlEncodedForm> {

        @Override
        public void apply(Supplier<UrlEncodedForm> supplier, HttpRequest.Builder builder) {
            UrlEncodedForm urlEncodedForm = supplier.get();
            if (Objects.nonNull(urlEncodedForm)) {
                builder.payloadPublisher(urlEncodedForm);
            }
        }
    }

    class FormMap implements RequestParameterHandler<Map> {

        @Override
        public void apply(Supplier<Map> supplier, HttpRequest.Builder builder) {
            Map formMap = supplier.get();
            if (Objects.nonNull(formMap)) {
                UrlEncodedForm.Builder formBuilder = UrlEncodedForm.newBuilder();
                formMap.forEach((k, v) -> formBuilder.add(String.valueOf(k), String.valueOf(v)));
                builder.payloadPublisher(formBuilder.build());
            }
        }
    }

    class Multipart implements RequestParameterHandler<MultipartData> {

        @Override
        public void apply(Supplier<MultipartData> supplier, HttpRequest.Builder builder) {
            MultipartData multipartData = supplier.get();
            if (Objects.nonNull(multipartData)) {
                builder.payloadPublisher(multipartData);
            }
        }
    }

    class Query implements RequestParameterHandler<String> {

        private final String queryName;

        private final boolean encoded;

        public Query(String queryName, boolean encoded) {
            this.queryName = queryName;
            this.encoded = encoded;
        }

        @Override
        public void apply(Supplier<String> supplier, HttpRequest.Builder builder) {
            String queryValue = supplier.get();
            if (Objects.nonNull(queryValue)) {
                if (encoded) {
                    String encodedName = HttpCodecUtils.X.encodeValue(queryName, StandardCharsets.UTF_8);
                    String encodedValue = HttpCodecUtils.X.encodeValue(queryValue, StandardCharsets.UTF_8);
                    builder.query(encodedName, encodedValue);
                } else {
                    builder.query(queryName, queryValue);
                }
            }
        }
    }

    class Queries implements RequestParameterHandler<Iterable<Pair>> {

        private final boolean encoded;

        public Queries(boolean encoded) {
            this.encoded = encoded;
        }

        @Override
        public void apply(Supplier<Iterable<Pair>> supplier, HttpRequest.Builder builder) {
            Iterable<Pair> queries = supplier.get();
            if (Objects.nonNull(queries)) {
                for (Pair pair : queries) {
                    String queryName = pair.name();
                    String queryValue = pair.value();
                    if (encoded) {
                        String encodedName = HttpCodecUtils.X.encodeValue(queryName, StandardCharsets.UTF_8);
                        String encodedValue = HttpCodecUtils.X.encodeValue(queryValue, StandardCharsets.UTF_8);
                        builder.query(encodedName, encodedValue);
                    } else {
                        builder.query(queryName, queryValue);
                    }
                }
            }
        }
    }

    class QueryMap implements RequestParameterHandler<Map> {

        private final boolean encoded;

        public QueryMap(boolean encoded) {
            this.encoded = encoded;
        }

        @Override
        public void apply(Supplier<Map> supplier, HttpRequest.Builder builder) {
            Map queryMap = supplier.get();
            if (Objects.nonNull(queryMap)) {
                queryMap.forEach((k, v) -> {
                    String queryName = String.valueOf(k);
                    String queryValue = String.valueOf(v);
                    if (encoded) {
                        String encodedName = HttpCodecUtils.X.encodeValue(queryName, StandardCharsets.UTF_8);
                        String encodedValue = HttpCodecUtils.X.encodeValue(queryValue, StandardCharsets.UTF_8);
                        builder.query(encodedName, encodedValue);
                    } else {
                        builder.query(queryName, queryValue);
                    }
                });
            }
        }
    }

    class PayloadSupport implements RequestParameterHandler<RequestPayloadSupport> {

        @Override
        public void apply(Supplier<RequestPayloadSupport> supplier, HttpRequest.Builder builder) {
            RequestPayloadSupport requestPayloadSupport = supplier.get();
            if (Objects.nonNull(requestPayloadSupport)) {
                builder.payloadPublisher(requestPayloadSupport);
            }
        }
    }

    class Payload<S> implements RequestParameterHandler<S> {

        private final Converter<S, RequestPayloadSupport> converter;

        public Payload(Converter<S, RequestPayloadSupport> converter) {
            this.converter = converter;
        }

        @Override
        public void apply(Supplier<S> supplier, HttpRequest.Builder builder) {
            S source = supplier.get();
            if (Objects.nonNull(source)) {
                RequestPayloadSupport payloadSupport = converter.convert(source);
                builder.payloadPublisher(payloadSupport);
            }
        }
    }

    class Var<T> implements RequestParameterHandler<T> {

        private final ApiMetadata.ApiVar var;

        private final String defaultValue;

        private volatile T varValue;

        public Var(String varName, String defaultValue) {
            this.var = ApiMetadata.ApiVar.fromVarName(varName);
            this.defaultValue = defaultValue;
        }

        @Override
        public void apply(Supplier<T> supplier, HttpRequest.Builder builder) {
            T var = supplier.get();
            if (Objects.nonNull(var)) {
                varValue = var;
            }
        }

        public ApiMetadata.ApiVar getVar() {
            return var;
        }

        public T getVarValue() {
            return Optional.ofNullable(varValue).orElse((T) var.getDefaultFunction().apply(defaultValue));
        }
    }
}
