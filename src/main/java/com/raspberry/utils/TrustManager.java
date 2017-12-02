package com.raspberry.utils;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class TrustManager implements X509TrustManager {

    private X509Certificate[] x509Certificates;

    public TrustManager(X509Certificate x509Certificate) {
        x509Certificates = new X509Certificate[1];
        x509Certificates[0] = x509Certificate;
    }

    public X509Certificate getCertificate() {
        return x509Certificates[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return x509Certificates;
    }
}
