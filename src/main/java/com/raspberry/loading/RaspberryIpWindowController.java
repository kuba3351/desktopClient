package com.raspberry.loading;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Kontroler obsługujący okienko do wpisania adresu Ip raspberry
 */
public class RaspberryIpWindowController {
    @FXML
    private TextField ipTextField;

    private void close() {
        ((Stage) ipTextField.getScene().getWindow()).close();
    }

    public void onCancelButtonClick() {
        System.exit(0);
    }

    public void onOkButtonClick() {
        AutoDiscovery.getInstance().setRaspberryIpAddress(ipTextField.getText());
        AutoDiscoveryRunner.getInstance().setFinished(true);
        close();
    }
}
