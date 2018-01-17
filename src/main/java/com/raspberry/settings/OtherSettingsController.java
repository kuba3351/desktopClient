package com.raspberry.settings;

import com.raspberry.CameraType;
import com.raspberry.dto.AutoPhotosDTO;
import com.raspberry.dto.PhotoDTO;
import com.raspberry.dto.RobotDTO;
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
import java.util.*;

/**
 * Kontroler odpowiedzialny za ustawienia rozdzielczości zdjęć i inne
 */
public class OtherSettingsController implements Initializable, LoadingTask, Clearable {

    private static OtherSettingsController instance;
    private Map<PhotoDTO, String> resolutionDTOMap;
    private PhotoDTO photoDTO;
    private AutoPhotosDTO autoPhotosDTO;

    @FXML
    private Label settingsChangedLabel;

    @FXML
    private ChoiceBox<String> resolution;

    @FXML
    private CheckBox autoPhotos;

    @FXML
    private Spinner<Integer> autoPhotosDist;

    @FXML
    private ChoiceBox<String> left;

    @FXML
    private ChoiceBox<String> right;

    @FXML
    private ChoiceBox<String> distanceSensor;

    @FXML
    private ChoiceBox<CameraType> cameraTypes;

    @FXML
    private CheckBox connect;

    @FXML CheckBox stopRobot;

    private RobotDTO robotDTO;

    private List<String> outputPorts;

    private List<String> inputPorts;

    private volatile boolean finished = false;

    public boolean isImportantSettingsChanged() {
        return importantSettingsChanged;
    }

    private boolean importantSettingsChanged;

    private OtherSettingsController() {
        outputPorts = new ArrayList<>();
        outputPorts.add("A");
        outputPorts.add("B");
        outputPorts.add("C");
        outputPorts.add("D");
        inputPorts = new ArrayList<>();
        inputPorts.add("S1");
        inputPorts.add("S2");
        inputPorts.add("S3");
        inputPorts.add("S4");
    }

    public static OtherSettingsController getInstance() {
        if (instance == null)
            instance = new OtherSettingsController();
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        importantSettingsChanged = false;
        settingsChangedLabel.setVisible(false);
        resolutionDTOMap = new HashMap<>();
        resolutionDTOMap.put(new PhotoDTO(640, 480), "VGA (640x480)");
        resolutionDTOMap.put(new PhotoDTO(800, 600), "SVGA (800x600)");
        resolutionDTOMap.put(new PhotoDTO(1024, 768), "XGA (1024x768)");
        resolutionDTOMap.put(new PhotoDTO(1366, 768), "HD READY(1366x768)");
        resolutionDTOMap.put(new PhotoDTO(1920, 1080), "FULL HD(1920x1080)");
        resolution.getItems().addAll(resolutionDTOMap.values());
        resolution.setValue(resolutionDTOMap.getOrDefault(photoDTO, "VGA (640x480)"));
        resolution.valueProperty().addListener((observableValue, s, t1) -> {
            onSettingsChanges();
            photoDTO = resolutionDTOMap.entrySet().stream().filter(entry -> entry.getValue().equals(t1)).map(Map.Entry::getKey).findFirst().orElse(null);
        });
        autoPhotos.setSelected(autoPhotosDTO.getAutoPhotosEnabled());
        autoPhotos.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            onSettingsChanges();
            autoPhotosDTO.setAutoPhotosEnabled(t1);
        });
        autoPhotosDist.setValueFactory(new SpinnerValues(1, 1000));
        autoPhotosDist.getValueFactory().setValue(autoPhotosDTO.getAutoPhotosDistance());
        autoPhotosDist.valueProperty().addListener((observableValue, integer, t1) -> {
            onSettingsChanges();
            autoPhotosDTO.setAutoPhotosDistance(t1);
        });
        left.getItems().addAll(outputPorts);
        left.setValue(robotDTO.getLeft());
        right.setValue(robotDTO.getRight());
        right.getItems().addAll(outputPorts);
        distanceSensor.getItems().addAll(inputPorts);
        distanceSensor.setValue(robotDTO.getDistanceSensor());
        connect.setSelected(robotDTO.getConnect());
        stopRobot.setSelected(robotDTO.getShouldStopOnPhotos());
        cameraTypes.getItems().addAll(CameraType.RASPBERRY, CameraType.USB);
        cameraTypes.setValue(photoDTO.getCameraType());
        left.valueProperty().addListener((observableValue, s, t1) -> {robotDTO.setLeft(t1); onSettingsChanges(); importantSettingsChanged = true;});
        right.valueProperty().addListener((observableValue, s, t1) -> {robotDTO.setRight(t1); onSettingsChanges(); importantSettingsChanged = true;});
        distanceSensor.valueProperty().addListener((observableValue, s, t1) -> {robotDTO.setDistanceSensor(t1); onSettingsChanges(); importantSettingsChanged = true;});
        connect.selectedProperty().addListener((observableValue, s, t1) -> {robotDTO.setConnect(t1); onSettingsChanges(); importantSettingsChanged = true;});
        cameraTypes.valueProperty().addListener((observableValue, s, t1) -> {photoDTO.setCameraType(t1); onSettingsChanges();});
        stopRobot.selectedProperty().addListener((observableValue, s, t1) -> {robotDTO.setShouldStopOnPhotos(t1); onSettingsChanges();});
    }

    public void onSettingsChanges() {
        settingsChangedLabel.setVisible(true);
    }

    @Override
    public boolean shouldBeExecuted() {
        return photoDTO == null || settingsChangedLabel.isVisible();
    }

    @Override
    public String getTaskName() {
        return photoDTO == null ? "Wczytuję inne ustawienia..." : "Zapisuję inne ustawienia...";
    }

    @Override
    public void execute() {
        if (photoDTO == null && autoPhotosDTO == null) {
            photoDTO = (PhotoDTO) Utils.getDTOFromServer("/api/photo", PhotoDTO.class);
            autoPhotosDTO = (AutoPhotosDTO) Utils.getDTOFromServer("/api/robot/autoPhotos", AutoPhotosDTO.class);
            robotDTO = (RobotDTO) Utils.getDTOFromServer("/api/robot", RobotDTO.class);
            finished = true;
        } else {
            photoDTO.setCameraType(cameraTypes.getValue());
            if (!Utils.saveDtoToServer("/api/photo", photoDTO) || !Utils.saveDtoToServer("/api/robot/autoPhotos", autoPhotosDTO) || !Utils.saveDtoToServer("/api/robot", robotDTO)) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd!");
                    alert.setHeaderText("Błąd ustawień");
                    alert.setContentText("Nie udało się zapisać ustawień");
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
        photoDTO = null;
        autoPhotosDTO = null;
        finished = false;
    }
}
