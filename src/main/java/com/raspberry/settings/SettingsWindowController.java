package com.raspberry.settings;

import com.raspberry.interfaces.LoadingTask;
import com.raspberry.loading.HelloController;
import com.raspberry.utils.Utils;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Kontroler odpowiedzialny za okienko z ustawieniami
 */
public class SettingsWindowController implements Initializable {
    @FXML
    private TabPane tabPane;

    public void onApplyButtonClick() {
        List<LoadingTask> tasks = Arrays
                .asList(SavingPlacesSettingsController.getInstance(),
                        SecurityConfigController.getInstance(),
                        NetworkConfigController.getInstance(),
                        OtherSettingsController.getInstance());
        Utils.openNewWindow("/fxml/settingsLoading.fxml", new HelloController(tasks,
                false, () -> {
        }), "Ładowanie", true, false, null);
    }

    public void onCancelButtonClick() {
        close();
    }

    public void onOkButtonClick() {
        List<LoadingTask> tasks = Arrays
                .asList(SavingPlacesSettingsController.getInstance(),
                        SecurityConfigController.getInstance(),
                        NetworkConfigController.getInstance(),
                        OtherSettingsController.getInstance(),
                        FinishSavingSettingsTask.getInstance());
        Utils.openNewWindow("/fxml/settingsLoading.fxml", new HelloController(tasks,
                false, this::close), "Ładowanie", true, false, null);
    }

    private void close() {
        Stage window = (Stage) tabPane.getScene().getWindow();
        window.getOnCloseRequest().handle(new WindowEvent(window, EventType.ROOT));
        window.close();
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
        tabs.get(4).setContent(Utils.loadTabContent("/fxml/otherSettings.fxml",
                OtherSettingsController.getInstance()));
    }
}
