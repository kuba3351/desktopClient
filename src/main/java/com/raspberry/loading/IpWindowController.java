package com.raspberry.loading;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class IpWindowController {
    @FXML
    private TextField ipTextField;

    private void close() {
        ((Stage)ipTextField.getScene().getWindow()).close();
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
