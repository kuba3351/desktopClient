package com.raspberry.settings;

import com.raspberry.dto.UsernameAndPasswordDTO;
import com.raspberry.interfaces.Clearable;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.loading.SecurityService;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Kontroler odpowiedzialny za ustawienia zabezpieczeń API
 */
public class SecurityConfigController implements Initializable, LoadingTask, Clearable {

    private static SecurityConfigController instance;
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

    private SecurityConfigController() {

    }

    public static SecurityConfigController getInstance() {
        if (instance == null)
            instance = new SecurityConfigController();
        return instance;
    }

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    public void setUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
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
        if (usernameAndPasswordDTO == null) {
            usernameAndPasswordDTO = (UsernameAndPasswordDTO) Utils.getDTOFromServer("/api/getAuthInfo", UsernameAndPasswordDTO.class);
            finished = true;
        } else {
            if (!Utils.saveDtoToServer("/api/saveAuthInfo", usernameAndPasswordDTO)) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd");
                    alert.setHeaderText("Błąd zabezpieczeń");
                    alert.setContentText("Nie udało się zapisać ustawień zabezpieczeń");
                    alert.showAndWait();
                    finished = true;
                });
            } else finished = true;
            SecurityService securityService = SecurityService.getInstance();
            securityService.setUsernameAndPasswordDTO(usernameAndPasswordDTO);
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
