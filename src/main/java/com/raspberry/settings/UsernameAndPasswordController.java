package com.raspberry.settings;

import com.raspberry.dto.UsernameAndPasswordDTO;
import com.raspberry.loading.SecurityService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Kontroler odpowiedzialny za okienko logowania pojawiające się gdy API jest zabezpieczone
 */
public class UsernameAndPasswordController {
    @FXML
    private TextField login;

    @FXML
    private PasswordField password;

    private void close() {
        ((Stage) login.getScene().getWindow()).close();
    }

    public void onCancelButtonClick() {
        close();
        System.exit(0);
    }

    public void onOkButtonClick() {
        UsernameAndPasswordDTO usernameAndPasswordDTO = new UsernameAndPasswordDTO();
        usernameAndPasswordDTO.setUsername(login.getText());
        usernameAndPasswordDTO.setPassword(password.getText());
        SecurityService securityService = SecurityService.getInstance();
        securityService.setUsernameAndPasswordDTO(usernameAndPasswordDTO);
        securityService.setFinished(true);
        close();
    }
}
