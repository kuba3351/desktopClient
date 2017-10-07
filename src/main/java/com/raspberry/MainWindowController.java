package com.raspberry;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    private ImageView preview;

    @FXML
    private Label photoLabel;

    @FXML
    private ProgressIndicator progress;

    @FXML
    private Label timeLabel;

    @FXML
    private Label ipLabel;

    @FXML
    private ProgressIndicator timeProgress;

    @FXML
    private Button startStopButton;

    public void setTimeDTO(TimeDTO timeDTO) {
        this.timeDTO = timeDTO;
        Platform.runLater(() -> timeLabel.setText(timeDTO.toString()));
    }

    private volatile TimeDTO timeDTO;

    private Thread timeThread;

    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    private static MainWindowController instance;

    public static MainWindowController getInstance() {
        if(instance == null)
            instance = new MainWindowController();
        return instance;
    }

    private MainWindowController() {

    }

    public void onTimeSettingsClick() {
        Utils.openNewWindow("/fxml/timeSettings.fxml", new TimeSettingsController(timeDTO, this), "Ustaw czas", false, false, null);
    }

    public void onOpenSettingsClick() {
        List<LoadingTask> tasks = Arrays
                .asList(SavingPlacesSettingsController.getInstance(),
                        SecurityConfigController.getInstance(),
                        NetworkConfigController.getInstance());
        Utils.openNewWindow("/fxml/settingsLoading.fxml", new HelloController(tasks,
                false, () -> Utils.openNewWindow("/fxml/settingsWindow.fxml", new SettingsWindowController(), "Ustawienia", false, false, (event -> {
                    tasks.stream().map(loadingTask -> (Clearable)loadingTask).forEach(Clearable::clear);
        }))), "Ładowanie", true, false, null);
    }

    public void onTakePhotoClick() {
        photoLabel.setVisible(false);
        progress.setVisible(true);
        preview.setVisible(false);
        Thread thread = new Thread(() -> {
            InputStream photoInputStream = Utils.getInputStreamFromServer("/api/takePhoto");
            Image image = new Image(photoInputStream);
            if(photoInputStream != null) {
                Platform.runLater(() -> {
                    progress.setVisible(false);
                    preview.setImage(image);
                    preview.setVisible(true);
                });
                if (ServerStateService.getInstance().getOveralStateDTO().getJpgComputerSaveEnabled()) {
                    String separator = null;
                    String jpgLocation = ServerStateService.getInstance().getOveralStateDTO().getJpgLocation();
                    String system = System.getProperty("os.name");
                    if(system.contains("linux") || system.contains("Linux"))
                        separator = "/";
                    else if(system.contains("Win") || system.contains("win"))
                        separator = "\\";
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", new File(jpgLocation+separator+ LocalDateTime.now().toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Błąd");
                            alert.setHeaderText("Błąd zapisu");
                            alert.setContentText("Nieudało się zapisać zdjęcia do pliku.\nSprawdź lokalizację, uprawnienia do zapisu i ilość wolnego miejsca na dysku.");
                            alert.showAndWait();
                        });
                    }
                }
            }

        });
        thread.start();
    }

    public void onStartStopButtonClick() {
        TimeThreadState timeThreadState = timeDTO.getTimeThreadState();
        if(timeThreadState.equals(TimeThreadState.NEW)) {
            Utils.performActionOnServer("/api/time/start");
            Platform.runLater(() -> timeLabel.setDisable(false));
            timeThread.start();
            startStopButton.setText("Stop");
            timeDTO.setTimeThreadState(TimeThreadState.RUNNING);
        }
        else if (timeThreadState.equals(TimeThreadState.SUSPENDED)) {
            Utils.performActionOnServer("/api/time/start");
            Platform.runLater(() -> timeLabel.setDisable(false));
            timeThread.resume();
            startStopButton.setText("Stop");
            timeDTO.setTimeThreadState(TimeThreadState.RUNNING);
        }
        else if (timeThreadState.equals(TimeThreadState.RUNNING)) {
            Utils.performActionOnServer("/api/time/stop");
            Platform.runLater(() -> timeLabel.setDisable(true));
            timeThread.suspend();
            startStopButton.setText("Start");
            timeDTO.setTimeThreadState(TimeThreadState.SUSPENDED);
        }
    }

    public void onStartPhoto() {
        Platform.runLater(() -> {
            photoLabel.setVisible(false);
            preview.setVisible(false);
            progress.setVisible(true);
        });
    }

    public void onPhotoFinished() {
        timeDTO.reset();
        Image image = new Image(Utils.getInputStreamFromServer("/api/getLastPhoto"));
        Platform.runLater(() -> {
            progress.setVisible(false);
            preview.setImage(image);
            preview.setVisible(true);
        });
        if (ServerStateService.getInstance().getOveralStateDTO().getJpgComputerSaveEnabled()) {
            String separator = null;
            String jpgLocation = ServerStateService.getInstance().getOveralStateDTO().getJpgLocation();
            String system = System.getProperty("os.name");
            if(system.contains("linux") || system.contains("Linux"))
                separator = "/";
            else if(system.contains("Win") || system.contains("win"))
                separator = "\\";
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(preview.getImage(), null), "jpg", new File(jpgLocation+separator+ LocalDateTime.now().toString()));
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd");
                    alert.setHeaderText("Błąd zapisu");
                    alert.setContentText("Nieudało się zapisać zdjęcia do pliku.\nSprawdź lokalizację, uprawnienia do zapisu i ilość wolnego miejsca na dysku.");
                    alert.showAndWait();
                });
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progress.setProgress(-1);
        progress.setVisible(false);
        timeLabel.setVisible(false);
        timeProgress.setProgress(-1);
        timeProgress.setVisible(true);
        ipLabel.setText(ipLabel.getText() + AutoDiscovery.getInstance().getRaspberryIpAddress());
        Thread thread = new Thread(() -> {
            timeDTO = (TimeDTO)Utils.getDTOFromServer("/api/time", TimeDTO.class);
            Platform.runLater(() -> {
                timeProgress.setVisible(false);
                timeLabel.setText(timeDTO.toString());
                timeLabel.setVisible(true);
                timeLabel.setDisable(!timeDTO.getTimeThreadState().equals(TimeThreadState.RUNNING));
            });
            if(timeDTO.getTimeThreadState().equals(TimeThreadState.RUNNING)) {
                timeThread.start();
                Platform.runLater(() -> startStopButton.setText("Stop"));
            }
        });
        thread.start();
        timeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    timeLabel.setText(timeDTO.toString());
                });
                timeDTO.tick();
            }
        });
        initialized = true;
    }
}
