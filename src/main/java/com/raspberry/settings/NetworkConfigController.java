package com.raspberry.settings;

import com.google.gson.internal.LinkedTreeMap;
import com.raspberry.dto.NetworkDTO;
import com.raspberry.interfaces.Clearable;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.loading.ServerStateService;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Kontroler ustawień sieci wi-fi
 */
public class NetworkConfigController implements Initializable, LoadingTask, Clearable {

    private static NetworkConfigController instance;
    @FXML
    private CheckBox hotspotMode;
    @FXML
    private TableView<LinkedTreeMap> visibleWifi;
    @FXML
    private Label networkName;
    @FXML
    private PasswordField password;
    @FXML
    private Label settingsChangedLabel;
    private volatile boolean finished = false;
    private NetworkDTO networkDTO;
    private List<LinkedTreeMap> networkViewDTOS;
    private Boolean hotspotEnabled;

    private NetworkConfigController() {

    }

    public static NetworkConfigController getInstance() {
        if (instance == null)
            instance = new NetworkConfigController();
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsChangedLabel.setVisible(false);
        TableColumn ssid = new TableColumn("Nazwa");
        ssid.setMinWidth(150);
        ssid.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                LinkedTreeMap<String, String> treeMap = (LinkedTreeMap<String, String>) cellDataFeatures.getValue();
                return new SimpleStringProperty(treeMap.get("ssid"));
            }
        });

        TableColumn bars = new TableColumn("Zasięg");
        bars.setMinWidth(150);
        bars.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                LinkedTreeMap<String, String> treeMap = (LinkedTreeMap<String, String>) cellDataFeatures.getValue();
                return new SimpleStringProperty(treeMap.get("bars"));
            }
        });

        TableColumn security = new TableColumn("Zabezpieczenia");
        security.setMinWidth(150);
        security.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures cellDataFeatures) {
                LinkedTreeMap<String, String> treeMap = (LinkedTreeMap<String, String>) cellDataFeatures.getValue();
                return new SimpleStringProperty(treeMap.get("security"));
            }
        });

        visibleWifi.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
                if(!t1.equals(-1)) {
                    String name = networkViewDTOS.get(t1.intValue()).get("ssid").toString();
                    networkName.setText(name);
                    networkDTO.setSsid(name);
                    settingsChanged();
                }
        });
        visibleWifi.getColumns().clear();
        visibleWifi.getColumns().addAll(ssid, bars, security);
        visibleWifi.getItems().addAll(networkViewDTOS);
        password.textProperty().addListener((observableValue, s, t1) -> {
            networkDTO.setPassword(password.getText());
            settingsChanged();
            password.setPromptText("(puste)");
        });
        hotspotMode.setSelected(hotspotEnabled);
        hotspotMode.selectedProperty().addListener((observableValue, aBoolean, t1) -> settingsChanged());
        if(!networkDTO.getHotspot())
            visibleWifi.getItems().stream().filter(item -> item.get("ssid").equals(networkDTO.getSsid())).findFirst().ifPresent(item -> visibleWifi.getSelectionModel().select(item));
        settingsChangedLabel.setVisible(false);
    }

    @Override
    public boolean shouldBeExecuted() {
        return networkDTO == null || settingsChangedLabel.isVisible();
    }

    @Override
    public String getTaskName() {
        if (networkDTO == null)
            return "Wczytuję konfigurację sieci...";
        else if (hotspotMode.isSelected())
            return "Stawiam hotspota...";
        else return "Przełączam do innej sieci...";
    }

    @Override
    public void execute() {
        if (networkDTO == null) {
            networkDTO = (NetworkDTO) Utils.getDTOFromServer("/api/network", NetworkDTO.class);
            hotspotEnabled = ServerStateService.getInstance().getOveralStateDTO().getHotspotEnabled();
            networkViewDTOS = (ArrayList<LinkedTreeMap>) Utils.getDTOFromServer("/api/network/checkAvailableWifi", ArrayList.class);
            finished = true;
        } else {
            networkDTO.setHotspot(hotspotMode.isSelected());
            if (!Utils.saveDtoToServer("/api/network", networkDTO)) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd");
                    alert.setHeaderText("Błąd komunikacji z serwerem");
                    alert.setContentText("Nie udało się przetworzyć ządania uruchomienia hotspota.");
                    alert.showAndWait();
                    finished = true;
                });
            } else finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public void onRefreshNetworksButtonClick() throws MalformedURLException {
        networkDTO = (NetworkDTO) Utils.getDTOFromServer("/api/network", NetworkDTO.class);
        visibleWifi.getItems().clear();
        initialize(new URL("http://"), new ResourceBundle() {
            @Override
            protected Object handleGetObject(String s) {
                return null;
            }

            @Override
            public Enumeration<String> getKeys() {
                return null;
            }
        });
    }

    public boolean areSettingsChanged() {
        return settingsChangedLabel.visibleProperty().get();
    }

    private void settingsChanged() {
        settingsChangedLabel.setVisible(true);
    }

    @Override
    public void clear() {
        networkDTO = null;
        finished = false;
    }
}
