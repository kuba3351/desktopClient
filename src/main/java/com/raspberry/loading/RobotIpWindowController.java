package com.raspberry.loading;

import com.raspberry.dto.RobotDTO;
import com.raspberry.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Klasa odpowiedzialna za okienko do wpisania adresu Ip robota
 */
public class RobotIpWindowController {
    @FXML
    private TextField ipTextField;

    private void close() {
        ((Stage) ipTextField.getScene().getWindow()).close();
    }

    public void onCancelButtonClick() {
        RobotConnection.getInstance().setFinished(true);
        close();
    }

    public void onOkButtonClick() {
        RobotDTO robotDTO = new RobotDTO();
        robotDTO.setIp(ipTextField.getText());
        if (Utils.saveDtoToServer("/api/robot/connectToRobot", robotDTO)) {
            RobotConnection.getInstance().setFinished(true);
            close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Łączenie z robotem");
            alert.setContentText("Nie udało się połączyć z robotem przy użyciu tego adresu IP.");
            alert.showAndWait();
        }
    }
}
