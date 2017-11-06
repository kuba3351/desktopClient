package com.raspberry.settings;

import com.raspberry.MainWindowController;
import com.raspberry.utils.SpinnerValues;
import com.raspberry.utils.Utils;
import com.raspberry.dto.TimeDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class TimeSettingsController implements Initializable {

    @FXML
    private Spinner<Integer> hour;

    @FXML
    private Spinner<Integer> minute;

    @FXML
    private Spinner<Integer> second;

    private TimeDTO timeDTO;

    private MainWindowController mainWindowController;

    public TimeSettingsController(TimeDTO timeDTO, MainWindowController mainWindowController) {
        this.timeDTO = timeDTO;
        this.mainWindowController = mainWindowController;
    }

    private void close() {
        ((Stage)hour.getScene().getWindow()).close();
    }

    public void onCancelButtonClick() {
        close();
    }

    public void onOkButtonClick() {
        timeDTO.setHours(hour.getValue());
        timeDTO.setMinutes(minute.getValue());
        timeDTO.setSeconds(second.getValue());
        timeDTO.reset();
        if(Utils.saveDtoToServer("/api/time", timeDTO)) {
            mainWindowController.setTimeDTO(timeDTO);
            close();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hour.setValueFactory(new SpinnerValues(0, 59));
        minute.setValueFactory(new SpinnerValues(0, 59));
        second.setValueFactory(new SpinnerValues(0, 59));
        hour.getValueFactory().setValue(timeDTO.getHours());
        minute.getValueFactory().setValue(timeDTO.getMinutes());
        second.getValueFactory().setValue(timeDTO.getSeconds());
    }

}
