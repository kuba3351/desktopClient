package com.raspberry;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import jdk.internal.util.xml.impl.ReaderUTF8;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.CharsetEncoder;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SecurityService implements LoadingTask {

    private volatile UsernameAndPasswordDTO usernameAndPasswordDTO;

    private String token;

    public String getToken() {
        return token;
    }

    private static SecurityService instance;

    public static SecurityService getInstance() {
        if(instance == null)
            instance = new SecurityService();
        return instance;
    }

    private SecurityService() {

    }

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    public void setUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
    }

    @Override
    public boolean shouldBeExecuted() {
        return ServerStateService.getInstance()
                .getOveralStateDTO().getSecurityEnabled();
    }

    @Override
    public String getTaskName() {
        return "Konfigurowanie zabezpieczeń";
    }

    @Override
    public void execute() {
        Platform.runLater(() -> {
            Utils.openNewWindow("/fxml/UsernameAndPassword.fxml", new UsernameAndPasswordController(), "Wpisz dane logowania", false, true);
        });
        while (usernameAndPasswordDTO == null) {

        }
        refreshToken();
        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);
        scheduler
                .scheduleAtFixedRate(this::refreshToken, 150, 150, TimeUnit.SECONDS);
    }

    private void refreshToken() {
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
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + "/getAuthToken").openConnection());
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
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = httpsURLConnection.getOutputStream();
            outputStream.write(new Gson().toJson(usernameAndPasswordDTO).getBytes());
            outputStream.close();
            InputStream inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new ReaderUTF8(inputStream));
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
        }
        token = new Gson().fromJson(reader, UsernameAndPasswordDTO.class).getToken();
    }
}
