package cn.vlts.solpic.core.config;

import lombok.Data;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.X509TrustManager;

/**
 * SSL config.
 *
 * @author throwable
 * @since 2024/8/6 星期二 17:39
 */
@Data
public final class SSLConfig {

    public static final SSLConfig NO = new SSLConfig();

    private SSLContext context;

    private SSLParameters params;

    private HostnameVerifier hostnameVerifier;

    private X509TrustManager trustManager;
}
