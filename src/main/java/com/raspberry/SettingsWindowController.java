package com.raspberry;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SettingsWindowController implements Initializable {
    @FXML
    private TabPane tabPane;

    public void onApplyButtonClick() {

    }

    public void onCancelButtonClick() {

    }

    public void onOkButtonClick() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Tab> tabs = tabPane.getTabs();
        tabs.get(0).setContent(Utils.loadTabContent("/fxml/savingPlacesSettings.fxml",
                SavingPlacesSettingsController.getInstance()));
        tabs.get(1).setContent(Utils.loadTabContent("/fxml/databaseSettings.fxml",
                DatabaseSettingsController.getInstance()));
        tabs.get(2).setContent(Utils.loadTabContent("/fxml/securityConfig.fxml",
                SecurityConfigController.getInstance()));
        tabs.get(3).setContent(Utils.loadTabContent("/fxml/wifiConfig.fxml",
                NetworkConfigController.getInstance()));
    }
}
