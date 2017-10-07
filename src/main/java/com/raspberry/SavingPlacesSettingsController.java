package com.raspberry;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SavingPlacesSettingsController implements Initializable, LoadingTask, Clearable {

    @FXML
    private CheckBox jpgComputerSave;

    @FXML
    private Button browseButton;

    @FXML
    private CheckBox jpgDatabaseSave;

    @FXML
    private CheckBox jpgPendriveSave;

    @FXML
    private CheckBox matDatabaseSave;

    @FXML
    private CheckBox matPendriveSave;

    @FXML
    private Label settingsChangedLabel;

    private SavingPlacesDTO savingPlacesDTO;

    public SavingPlacesDTO getSavingPlacesDTO() {
        return savingPlacesDTO;
    }

    private volatile boolean finished = false;

    public void setSavingPlacesDTO(SavingPlacesDTO savingPlacesDTO) {
        this.savingPlacesDTO = savingPlacesDTO;
    }

    private static SavingPlacesSettingsController instance;

    public static SavingPlacesSettingsController getInstance() {
        if(instance == null)
            instance = new SavingPlacesSettingsController();
        return instance;
    }

    private SavingPlacesSettingsController() {

    }

    public void onBrowseButtoClick() {
        onSettingsChanges();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String jpgComputerLocation = savingPlacesDTO.getJpgComputerLocation();
        if(jpgComputerLocation != null)
            directoryChooser.setInitialDirectory(new File(jpgComputerLocation));
        directoryChooser.setTitle("Wybierz folder");
        File directory = directoryChooser.showDialog(new Stage());
        if(directory != null)
            savingPlacesDTO.setJpgComputerLocation(directory.toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsChangedLabel.setVisible(false);
        jpgComputerSave.setSelected(savingPlacesDTO.getJpgComputerSave());
        jpgPendriveSave.setSelected(savingPlacesDTO.getJpgRaspberryPendriveSave());
        jpgPendriveSave.selectedProperty().addListener((observableValue, aBoolean, t1) -> savingPlacesDTO.setJpgRaspberryPendriveSave(t1));
        jpgDatabaseSave.setSelected(savingPlacesDTO.getJpgDatabaseSave());
        jpgDatabaseSave.selectedProperty().addListener((observableValue, aBoolean, t1) -> savingPlacesDTO.setJpgDatabaseSave(t1));
        matDatabaseSave.setSelected(savingPlacesDTO.getMatDatabaseSave());
        matDatabaseSave.selectedProperty().addListener((observableValue, aBoolean, t1) -> savingPlacesDTO.setMatDatabaseSave(t1));
        matPendriveSave.setSelected(savingPlacesDTO.getMatPendriveSave());
        matPendriveSave.selectedProperty().addListener((observableValue, aBoolean, t1) -> savingPlacesDTO.setMatPendriveSave(t1));
        browseButton.setDisable(!savingPlacesDTO.getJpgComputerSave());
        jpgComputerSave.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            browseButton.setDisable(!t1);
            savingPlacesDTO.setJpgComputerSave(t1);
        });
    }

    public void onSettingsChanges() {
        settingsChangedLabel.setVisible(true);
    }

    @Override
    public boolean shouldBeExecuted() {
        return savingPlacesDTO == null || settingsChangedLabel.isVisible() || DatabaseSettingsController.getInstance().getSettingsChangedLabel().isVisible();
    }

    @Override
    public String getTaskName() {
        return savingPlacesDTO == null ? "Wczytuję konfigurację miejsc zapisu..." : "Zapisuję konfigurację miejsc zapisu...";
    }

    @Override
    public void execute() {
        if(savingPlacesDTO == null) {
            savingPlacesDTO = (SavingPlacesDTO) Utils.getDTOFromServer("/api/savingPlaces", SavingPlacesDTO.class);
            DatabaseSettingsController.getInstance().setDatabaseConfigDTO(savingPlacesDTO.getDatabaseConfig());
            finished = true;
        }
        else {
            if(!Utils.saveDtoToServer("/api/savingPlaces", savingPlacesDTO)) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd!");
                    alert.setHeaderText("Błąd miejsc zapisu");
                    alert.setContentText("Nie udało się zapisać konfiguracji miejsc zapisu.");
                    alert.showAndWait();
                    finished = true;
                });
            }
            else finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void clear() {
        savingPlacesDTO = null;
        finished = false;
    }
}
