package com.raspberry.settings;

import com.raspberry.dto.AutoPhotosDTO;
import com.raspberry.dto.PhotoResolutionDTO;
import com.raspberry.interfaces.Clearable;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.utils.SpinnerValues;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Kontroler odpowiedzialny za ustawienia rozdzielczości zdjęć i inne
 */
public class OtherSettingsController implements Initializable, LoadingTask, Clearable {

    private static OtherSettingsController instance;
    private Map<PhotoResolutionDTO, String> resolutionDTOMap;
    private PhotoResolutionDTO photoResolutionDTO;
    private AutoPhotosDTO autoPhotosDTO;

    @FXML
    private Label settingsChangedLabel;

    @FXML
    private ChoiceBox<String> resolution;

    @FXML
    private CheckBox autoPhotos;

    @FXML
    private Spinner<Integer> autoPhotosDist;

    private volatile boolean finished = false;

    private OtherSettingsController() {

    }

    public static OtherSettingsController getInstance() {
        if (instance == null)
            instance = new OtherSettingsController();
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsChangedLabel.setVisible(false);
        resolutionDTOMap = new HashMap<>();
        resolutionDTOMap.put(new PhotoResolutionDTO(640, 480), "VGA (640x480)");
        resolutionDTOMap.put(new PhotoResolutionDTO(800, 600), "SVGA (800x600)");
        resolutionDTOMap.put(new PhotoResolutionDTO(1024, 768), "XGA (1024x768)");
        resolutionDTOMap.put(new PhotoResolutionDTO(1366, 768), "HD READY(1366x768)");
        resolutionDTOMap.put(new PhotoResolutionDTO(1920, 1080), "FULL HD(1920x1080)");
        resolution.getItems().addAll(resolutionDTOMap.values());
        resolution.setValue(resolutionDTOMap.getOrDefault(photoResolutionDTO, "VGA (640x480)"));
        resolution.valueProperty().addListener((observableValue, s, t1) -> {
            settingsChangedLabel.setVisible(true);
            photoResolutionDTO = resolutionDTOMap.entrySet().stream().filter(entry -> entry.getValue().equals(t1)).map(Map.Entry::getKey).findFirst().orElse(null);
        });
        autoPhotos.setSelected(autoPhotosDTO.getAutoPhotosEnabled());
        autoPhotos.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            settingsChangedLabel.setVisible(true);
            autoPhotosDTO.setAutoPhotosEnabled(t1);
        });
        autoPhotosDist.setValueFactory(new SpinnerValues(1, 20));
        autoPhotosDist.getValueFactory().setValue(autoPhotosDTO.getAutoPhotosDistance());
        autoPhotosDist.valueProperty().addListener((observableValue, integer, t1) -> {
            settingsChangedLabel.setVisible(true);
            autoPhotosDTO.setAutoPhotosDistance(t1);
        });
    }

    public void onSettingsChanges() {
        settingsChangedLabel.setVisible(true);
    }

    @Override
    public boolean shouldBeExecuted() {
        return photoResolutionDTO == null || settingsChangedLabel.isVisible();
    }

    @Override
    public String getTaskName() {
        return photoResolutionDTO == null ? "Wczytuję inne ustawienia..." : "Zapisuję inne ustawienia...";
    }

    @Override
    public void execute() {
        if (photoResolutionDTO == null && autoPhotosDTO == null) {
            photoResolutionDTO = (PhotoResolutionDTO) Utils.getDTOFromServer("/api/photo/resolution", PhotoResolutionDTO.class);
            autoPhotosDTO = (AutoPhotosDTO) Utils.getDTOFromServer("/api/robot/autoPhotos", AutoPhotosDTO.class);
            finished = true;
        } else {
            if (!Utils.saveDtoToServer("/api/photo/resolution", photoResolutionDTO) || !Utils.saveDtoToServer("/api/robot/autoPhotos", autoPhotosDTO)) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd!");
                    alert.setHeaderText("Błąd tozdzielczości");
                    alert.setContentText("Nie udało się zapisać nowej rozdzielczości");
                    alert.showAndWait();
                    finished = true;
                });
            } else finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void clear() {
        photoResolutionDTO = null;
        autoPhotosDTO = null;
        finished = false;
    }
}
