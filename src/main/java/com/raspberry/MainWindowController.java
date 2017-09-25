package com.raspberry;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    private ImageView preview;

    @FXML
    private Label photoLabel;

    @FXML
    private ProgressIndicator progress;

    @FXML
    private Label timeLabek;

    public void onOpenSettingsClick() {
        Utils.openNewWindow("/fxml/settingsLoading.fxml", new HelloController(Arrays
                        .asList(SavingPlacesSettingsController.getInstance(),
                                SecurityConfigController.getInstance(),
                                NetworkConfigController.getInstance()),
                false, () -> Utils.openNewWindow("/fxml/settingsWindow.fxml", new SettingsWindowController(), "Ustawienia", false, false)), "Ładowanie", true, false);
    }

    public void onTakePhotoClick() {
        photoLabel.setVisible(false);
        progress.setVisible(true);
        preview.setVisible(false);
        Thread thread = new Thread(() -> {
            InputStream photoInputStream = Utils.getInputStreamFromServer("/api/takePhoto");
            if(photoInputStream != null)
                Platform.runLater(() -> {
                    progress.setVisible(false);
                    preview.setImage(new Image(photoInputStream));
                    preview.setVisible(true);
                });
        });
        thread.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progress.setProgress(-1);
        progress.setVisible(false);
    }
}
