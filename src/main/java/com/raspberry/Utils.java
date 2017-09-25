package com.raspberry;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jdk.internal.util.xml.impl.ReaderUTF8;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class Utils {
    public static void openNewWindow(String fxmlFile, Object controller, String title, boolean undecorated, boolean wait) {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(controller);
        Parent rootNode = null;
        try {
            rootNode = loader.load(Utils.class.getResourceAsStream(fxmlFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        Scene scene = new Scene(rootNode);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        if (undecorated)
            stage.initStyle(StageStyle.UNDECORATED);
        if (!wait)
            stage.show();
        else
            stage.showAndWait();
    }

    public static Parent loadTabContent(String fxmlFile, Object controller) {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(controller);
        try {
            return loader.load(Utils.class.getResourceAsStream(fxmlFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public static Object getDTOFromServer(String address, Class clazz) {
        KeyStore p12;
        Enumeration e;
        HttpsURLConnection httpsURLConnection;
        SSLContext ssl;
        X509Certificate certificate;
        Reader reader = null;
        try {
            p12 = KeyStore.getInstance("pkcs12");
            p12.load(ServerStateService.class.getResourceAsStream("/keystore.p12"), "Jakubs12343351577@".toCharArray());
            e = p12.aliases();
            String alias = (String) e.nextElement();
            certificate = (X509Certificate) p12.getCertificate(alias);
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + address).openConnection());
            ssl = SSLContext.getInstance("SSL");
            X509Certificate finalCertificate1 = certificate;
            ssl.init(new KeyManager[]{
                    new KeyManager() {
                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public boolean equals(Object o) {
                            return super.equals(o);
                        }

                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }

                        @Override
                        public String toString() {
                            return super.toString();
                        }

                        @Override
                        protected void finalize() throws Throwable {
                            super.finalize();
                        }
                    }
            }, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{
                                    finalCertificate1
                            };
                        }
                    }
            }, new SecureRandom());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(certificate));
            if(SecurityService.getInstance().getToken() != null)
                httpsURLConnection.addRequestProperty("auth.token",
                        SecurityService.getInstance().getToken());
            InputStream inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new ReaderUTF8(inputStream));
        } catch (KeyManagementException | KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd");
                alert.setHeaderText("Błąd połączenia z serwerem");
                if (ex instanceof IOException)
                    alert.setContentText("Wystąpił błąd połączenia z serwerem. Być może konieczne jet uaktualnienie klienta lub serwera do nowszej wersji.");
                else
                    alert.setContentText("Wystąpił problem podczas weryfikacji certyfikatu. Przyczyną może być uszkodzenie pliku z certyfikatem dostarczonego wraz z klientem lub próba przechwycenia komunikacji między klientem i serwerem.");
                alert.showAndWait();
                System.exit(0);
            });
        }
        return new Gson().fromJson(reader, clazz);
    }

    public static InputStream getInputStreamFromServer(String address) {
        KeyStore p12;
        Enumeration e;
        HttpsURLConnection httpsURLConnection = null;
        SSLContext ssl;
        X509Certificate certificate;
        Reader reader = null;
        try {
            p12 = KeyStore.getInstance("pkcs12");
            p12.load(ServerStateService.class.getResourceAsStream("/keystore.p12"), "Jakubs12343351577@".toCharArray());
            e = p12.aliases();
            String alias = (String) e.nextElement();
            certificate = (X509Certificate) p12.getCertificate(alias);
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + address).openConnection());
            ssl = SSLContext.getInstance("SSL");
            X509Certificate finalCertificate1 = certificate;
            ssl.init(new KeyManager[]{
                    new KeyManager() {
                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public boolean equals(Object o) {
                            return super.equals(o);
                        }

                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }

                        @Override
                        public String toString() {
                            return super.toString();
                        }

                        @Override
                        protected void finalize() throws Throwable {
                            super.finalize();
                        }
                    }
            }, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{
                                    finalCertificate1
                            };
                        }
                    }
            }, new SecureRandom());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(certificate));
            if(SecurityService.getInstance().getToken() != null)
                httpsURLConnection.addRequestProperty("auth.token",
                        SecurityService.getInstance().getToken());
            return httpsURLConnection.getInputStream();
        } catch (KeyManagementException | KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Błąd połączenia z serwerem");
            if (ex instanceof IOException)
                alert.setContentText("Wystąpił błąd połączenia z serwerem. Być może konieczne jet uaktualnienie klienta lub serwera do nowszej wersji.");
            else
                alert.setContentText("Wystąpił problem podczas weryfikacji certyfikatu. Przyczyną może być uszkodzenie pliku z certyfikatem dostarczonego wraz z klientem lub próba przechwycenia komunikacji między klientem i serwerem.");
            alert.showAndWait();
            System.exit(0);
            return null;
        }
    }

    public static Boolean performActionOnServer(String address) {
        KeyStore p12;
        Enumeration e;
        HttpsURLConnection httpsURLConnection;
        SSLContext ssl;
        X509Certificate certificate;
        try {
            p12 = KeyStore.getInstance("pkcs12");
            p12.load(ServerStateService.class.getResourceAsStream("/keystore.p12"), "Jakubs12343351577@".toCharArray());
            e = p12.aliases();
            String alias = (String) e.nextElement();
            certificate = (X509Certificate) p12.getCertificate(alias);
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + address).openConnection());
            ssl = SSLContext.getInstance("SSL");
            X509Certificate finalCertificate = certificate;
            ssl.init(new KeyManager[]{
                    new KeyManager() {
                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public boolean equals(Object o) {
                            return super.equals(o);
                        }

                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }

                        @Override
                        public String toString() {
                            return super.toString();
                        }

                        @Override
                        protected void finalize() throws Throwable {
                            super.finalize();
                        }
                    }
            }, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{
                                    finalCertificate
                            };
                        }
                    }
            }, new SecureRandom());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(finalCertificate));
            if(SecurityService.getInstance().getToken() != null)
                httpsURLConnection.addRequestProperty("auth.token",
                        SecurityService.getInstance().getToken());
            return httpsURLConnection.getResponseCode() == 200;
        } catch (KeyManagementException | KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Błąd połączenia z serwerem");
            if (ex instanceof IOException)
                alert.setContentText("Wystąpił błąd połączenia z serwerem. Być może konieczne jet uaktualnienie klienta lub serwera do nowszej wersji.");
            else
                alert.setContentText("Wystąpił problem podczas weryfikacji certyfikatu. Przyczyną może być uszkodzenie pliku z certyfikatem dostarczonego wraz z klientem lub próba przechwycenia komunikacji między klientem i serwerem.");
            alert.showAndWait();
            System.exit(0);
            return false;
        }
    }
}