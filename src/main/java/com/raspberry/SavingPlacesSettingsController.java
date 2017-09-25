package com.raspberry;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ResourceBundle;

public class SavingPlacesSettingsController implements Initializable, LoadingTask {

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

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jpgComputerSave.setSelected(savingPlacesDTO.getJpgComputerSave());
        jpgPendriveSave.setSelected(savingPlacesDTO.getJpgRaspberryPendriveSave());
        jpgDatabaseSave.setSelected(savingPlacesDTO.getJpgDatabaseSave());
        matDatabaseSave.setSelected(savingPlacesDTO.getMatDatabaseSave());
        matPendriveSave.setSelected(savingPlacesDTO.getMatPendriveSave());
        browseButton.setDisable(!savingPlacesDTO.getJpgComputerSave());
        jpgComputerSave.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                browseButton.setDisable(!t1);
            }
        });
    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }

    @Override
    public String getTaskName() {
        return "Wczytuję konfigurację miejsc zapisu...";
    }

    @Override
    public void execute() {
        savingPlacesDTO = (SavingPlacesDTO)Utils.getDTOFromServer("/api/savingPlaces", SavingPlacesDTO.class);
        DatabaseSettingsController.getInstance().setDatabaseConfigDTO(savingPlacesDTO.getDatabaseConfig());
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
