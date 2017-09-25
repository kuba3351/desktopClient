package com.raspberry;

import com.google.gson.internal.LinkedTreeMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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

public class NetworkConfigController implements Initializable, LoadingTask {
    @FXML
    private CheckBox hotspotMode;

    @FXML
    private TableView<LinkedTreeMap> visibleWifi;

    @FXML
    private Label networkName;

    @FXML
    private PasswordField password;

    private volatile boolean finished = false;

    private NetworkDTO networkDTO;

    private List<LinkedTreeMap> networkViewDTOS;

    public List<LinkedTreeMap> getNetworkViewDTOS() {
        return networkViewDTOS;
    }

    public void setNetworkViewDTOS(List<LinkedTreeMap> networkViewDTOS) {
        this.networkViewDTOS = networkViewDTOS;
    }

    public NetworkDTO getNetworkDTO() {
        return networkDTO;
    }

    public void setNetworkDTO(NetworkDTO networkDTO) {
        this.networkDTO = networkDTO;
    }

    private Boolean hotspotEnabled;

    public Boolean getHotspotEnabled() {
        return hotspotEnabled;
    }

    public void setHotspotEnabled(Boolean hotspotEnabled) {
        this.hotspotEnabled = hotspotEnabled;
    }

    private static NetworkConfigController instance;

    public static NetworkConfigController getInstance() {
        if(instance == null)
            instance = new NetworkConfigController();
        return instance;
    }

    private NetworkConfigController() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

        visibleWifi.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                String name = networkViewDTOS.get(t1.intValue()).get("ssid").toString();
                networkName.setText(name);
                networkDTO.setSsid(name);
            }
        });
        visibleWifi.getColumns().clear();
        visibleWifi.getColumns().addAll(ssid, bars, security);
        visibleWifi.getItems().addAll(networkViewDTOS);
    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }

    @Override
    public String getTaskName() {
        return "Wczytuję konfigurację sieci...";
    }

    @Override
    public void execute() {
        networkDTO = (NetworkDTO)Utils.getDTOFromServer("/api/network/getNetworkInfo", NetworkDTO.class);
        hotspotEnabled = ServerStateService.getInstance().getOveralStateDTO().getHotspotEnabled();
        networkViewDTOS = (ArrayList<LinkedTreeMap>)Utils.getDTOFromServer("/api/network/checkAvailableWifi", ArrayList.class);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public void onRefreshNetworksButtonClick() throws MalformedURLException {
        networkDTO = (NetworkDTO)Utils.getDTOFromServer("/api/network/getNetworkInfo", NetworkDTO.class);
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
}
