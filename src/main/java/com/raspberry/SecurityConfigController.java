package com.raspberry;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SecurityConfigController implements Initializable, LoadingTask, Clearable {

    @FXML
    private CheckBox securityEnabled;

    @FXML
    private TextField userName;

    @FXML
    private PasswordField password;

    @FXML
    private Label settingsChangedLabel;

    private UsernameAndPasswordDTO usernameAndPasswordDTO;

    private volatile boolean finished = false;

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    public void setUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
    }

    private static SecurityConfigController instance;

    public static SecurityConfigController getInstance() {
        if(instance == null)
            instance = new SecurityConfigController();
        return instance;
    }

    private SecurityConfigController() {

    }

    public boolean areSettingsChanged() {
        return settingsChangedLabel.visibleProperty().get();
    }

    private void settingsChanged() {
        settingsChangedLabel.setVisible(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsChangedLabel.setVisible(false);
        securityEnabled.setSelected(usernameAndPasswordDTO.getEnabled());
        securityEnabled.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            usernameAndPasswordDTO.setEnabled(t1);
            settingsChanged();
        });
        userName.setText(usernameAndPasswordDTO.getUsername());
        userName.textProperty().addListener((observableValue, s, t1) -> {
            usernameAndPasswordDTO.setUsername(t1);
            settingsChanged();
        });
        password.textProperty().addListener((observableValue, s, t1) -> {
            usernameAndPasswordDTO.setPassword(t1.equals("") ? null : t1);
            settingsChanged();
        });
    }

    @Override
    public boolean shouldBeExecuted() {
        return usernameAndPasswordDTO == null || settingsChangedLabel.isVisible();
    }

    @Override
    public String getTaskName() {
        return usernameAndPasswordDTO == null ? "Wczytuję konfigurację zabezpieczeń..." : "Zapisuję konfigurację zabezpieczeń...";
    }

    @Override
    public void execute() {
        if(usernameAndPasswordDTO == null) {
            usernameAndPasswordDTO = (UsernameAndPasswordDTO) Utils.getDTOFromServer("/api/getAuthInfo", UsernameAndPasswordDTO.class);
            finished = true;
        }
        else {
            if(!Utils.saveDtoToServer("/api/saveAuthInfo", usernameAndPasswordDTO)) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd");
                    alert.setHeaderText("Błąd zabezpieczeń");
                    alert.setContentText("Nie udało się zapisać ustawień zabezpieczeń");
                    alert.showAndWait();
                    finished = true;
                });
            }
            else finished = true;
            SecurityService securityService = SecurityService.getInstance();
            securityService.setUsernameAndPasswordDTO(usernameAndPasswordDTO);
            securityService.refreshToken();
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void clear() {
        usernameAndPasswordDTO = null;
        finished = false;
    }
}
