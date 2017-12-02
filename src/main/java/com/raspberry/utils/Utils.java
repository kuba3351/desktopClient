package com.raspberry.utils;

import com.google.gson.Gson;
import com.raspberry.dto.UsernameAndPasswordDTO;
import com.raspberry.loading.AutoDiscovery;
import com.raspberry.loading.SecurityService;
import com.raspberry.loading.ServerStateService;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Klasa z narzędziami do często wykonywanych czynności
 */
public class Utils {

    private static FakeKeyManager[] fakeKeyManagers;
    private static TrustManager[] trustManagers;
    private static SSLContext ssl;

    //statyczny blok inicjalizujący mechanizm sprawdzania poprawności certyfikatu
    static {
        try {
            KeyStore p12 = KeyStore.getInstance("pkcs12");
            p12.load(ServerStateService.class.getResourceAsStream("/keystore.p12"), "Jakubs12343351577@".toCharArray());
            Enumeration<String> aliases = p12.aliases();
            String alias = aliases.nextElement();
            fakeKeyManagers = new FakeKeyManager[1];
            fakeKeyManagers[0] = new FakeKeyManager();
            trustManagers = new TrustManager[1];
            trustManagers[0] = new TrustManager((X509Certificate) p12.getCertificate(alias));
            ssl = SSLContext.getInstance("SSL");
            ssl.init(fakeKeyManagers, trustManagers, new SecureRandom());
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda do otwierania nowego okienka
     *
     * @param fxmlFile    ścieżka do pliku fxml z definicją okienka i kontrolkami
     * @param controller  obiekt klasy będącej kontrolerem okienka
     * @param title       tytuł okienka
     * @param undecorated czy okienko ma być bez paska u góry? (używane do okienek z paskiem postępu)
     * @param wait        czy czekać na zamknięcie okienka?
     * @param event       fukncja do wykonania przy zamknięciu krzyżykiem
     * @return nowo otwarte okienko
     */
    public static Stage openNewWindow(String fxmlFile, Object controller, String title, boolean undecorated, boolean wait, EventHandler<WindowEvent> event) {
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
        if (event != null)
            stage.setOnCloseRequest(event);
        if (undecorated)
            stage.initStyle(StageStyle.UNDECORATED);
        if (!wait)
            stage.show();
        else
            stage.showAndWait();
        return stage;
    }

    /**
     * Klasa ładująca widok do kontenera. Używane w okienku z ustawieniami do ładowania poszczególnych kart ustawień
     *
     * @param fxmlFile   ścieżka do pliku fxml z definicją kontrolek
     * @param controller klasa kontrolera
     * @return
     */
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

    /**
     * Metoda służąca do pobrania obiektu klasy DTO z serwera w formie JSONa i deserializację do obiektu określonego typu
     *
     * @param address adres żądania
     * @param clazz   typ zdeserializowanego obiektu
     * @return zdeserializowany obiekt
     */
    public static Object getDTOFromServer(String address, Class clazz) {
        HttpsURLConnection httpsURLConnection;
        Reader reader = null;
        try {
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + address).openConnection());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(trustManagers[0].getCertificate()));
            if (SecurityService.getInstance().getToken() != null)
                httpsURLConnection.addRequestProperty("auth.token",
                        SecurityService.getInstance().getToken());
            InputStream inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException ex) {
            ex.printStackTrace();
            showErrorAndCloseApp();
        }
        return new Gson().fromJson(reader, clazz);
    }

    /**
     * Metoda służąca do serializacji obiektu klasy DTO do JSONa i wysłania na serwer
     *
     * @param address adres żądania
     * @param dto     obiekt do wysłania
     * @return czy się udało, i serwer zwrócił kod 200?
     */
    public static Boolean saveDtoToServer(String address, Object dto) {
        HttpsURLConnection httpsURLConnection;
        try {
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + address).openConnection());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(trustManagers[0].getCertificate()));
            if (SecurityService.getInstance().getToken() != null)
                httpsURLConnection.addRequestProperty("auth.token",
                        SecurityService.getInstance().getToken());
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.getOutputStream().write(new Gson().toJson(dto).getBytes());
            return httpsURLConnection.getResponseCode() == 200;
        } catch (IOException ex) {
            ex.printStackTrace();
            showErrorAndCloseApp();
            return false;
        }
    }

    /**
     * metoda do pobierania danych z serwera za pomocą strumienia. Używana do pobierania podglądu zdjęć
     *
     * @param address adres żądania
     * @return strumień z danymi
     */
    public static InputStream getInputStreamFromServer(String address) {
        HttpsURLConnection httpsURLConnection;
        try {
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + address).openConnection());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(trustManagers[0].getCertificate()));
            if (SecurityService.getInstance().getToken() != null)
                httpsURLConnection.addRequestProperty("auth.token",
                        SecurityService.getInstance().getToken());
            return httpsURLConnection.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
            showErrorAndCloseApp();
            return null;
        }
    }

    /**
     * metoda używana do wykonania akcji na serwerze niewymagającej przesyłania JSONów ani innych dodatkowych danych
     *
     * @param address adres requesta
     * @return czy się udało i serwer zwrócił kod 200?
     */
    public static Boolean performActionOnServer(String address) {
        HttpsURLConnection httpsURLConnection;
        try {
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + address).openConnection());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(trustManagers[0].getCertificate()));
            if (SecurityService.getInstance().getToken() != null)
                httpsURLConnection.addRequestProperty("auth.token",
                        SecurityService.getInstance().getToken());
            return httpsURLConnection.getResponseCode() == 200;
        } catch (IOException ex) {
            ex.printStackTrace();
            showErrorAndCloseApp();
            return false;
        }
    }

    private static void showErrorAndCloseApp() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Błąd połączenia z serwerem");
            alert.setContentText("Wystąpił błąd połączenia z serwerem. Być może konieczne jet uaktualnienie klienta lub serwera do nowszej wersji.");
            alert.showAndWait();
            System.exit(0);
        });
    }

    /**
     * Metoda służąca do odświeżania tokena autoryzacyjnego gdy API jest zabezpieczone
     *
     * @param usernameAndPasswordDTO nazwa użytkownika i hasło
     * @return nowy token autoryzacyjny
     */
    public static String refreshToken(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        HttpsURLConnection httpsURLConnection;
        Reader reader = null;
        try {
            AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
            httpsURLConnection = ((HttpsURLConnection) new URL("https://" + autoDiscovery.getRaspberryIpAddress() + "/getAuthToken").openConnection());
            httpsURLConnection.setSSLSocketFactory(ssl.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(new CertificateVerifer(trustManagers[0].getCertificate()));
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = httpsURLConnection.getOutputStream();
            outputStream.write(new Gson().toJson(usernameAndPasswordDTO).getBytes());
            outputStream.close();
            InputStream inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd");
                alert.setHeaderText("Błąd połączenia z serwerem");
                alert.setContentText("Wystąpił błąd połączenia z serwerem. Przyczyn może być wiele. Jeśli błąd wystąpił po podaniu danych autoryzacyjnych, sprawdź ich poprawność.");
                alert.showAndWait();
                System.exit(0);
            });
        }
        return new Gson().fromJson(reader, UsernameAndPasswordDTO.class).getToken();
    }
}