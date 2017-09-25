package com.raspberry;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SecurityConfigController implements Initializable, LoadingTask {

    @FXML
    private CheckBox securityEnabled;

    @FXML
    private TextField userName;

    @FXML
    private PasswordField password;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        securityEnabled.setSelected(usernameAndPasswordDTO.getEnabled());
        userName.setText(usernameAndPasswordDTO.getUsername());
    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }

    @Override
    public String getTaskName() {
        return "Wczytuję konfigurację zabezpieczeń...";
    }

    @Override
    public void execute() {
        usernameAndPasswordDTO = (UsernameAndPasswordDTO)Utils.getDTOFromServer("/api/getAuthInfo", UsernameAndPasswordDTO.class);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
