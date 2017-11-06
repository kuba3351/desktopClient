package com.raspberry.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;

public class CertificateVerifer implements HostnameVerifier {
        private final X509Certificate finalCertificate;

        public CertificateVerifer(X509Certificate finalCertificate) {
            this.finalCertificate = finalCertificate;
        }

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            boolean valid = false;
            try {
                valid = isValid(s, sslSession);
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            }
            return valid;
        }

    private boolean isValid(String s, SSLSession sslSession) throws SSLPeerUnverifiedException {
        return sslSession.getCipherSuite().equals("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256")
                && sslSession.getPeerCertificates()[0].equals(finalCertificate);
    }
}
