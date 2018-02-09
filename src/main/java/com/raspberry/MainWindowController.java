package com.raspberry;

import com.raspberry.dto.TimeDTO;
import com.raspberry.interfaces.Clearable;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.loading.AutoDiscovery;
import com.raspberry.loading.HelloController;
import com.raspberry.loading.RabbitConnector;
import com.raspberry.loading.ServerStateService;
import com.raspberry.settings.*;
import com.raspberry.utils.Utils;
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
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Kontroler odpowiedzialny za główne okienko
 */
public class MainWindowController implements Initializable {

    private static MainWindowController instance;
    @FXML
    private ImageView preview1;
    @FXML
    private ImageView preview2;
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
    @FXML
    private Button left;
    @FXML
    private Button right;
    private volatile TimeDTO timeDTO;

    private Thread timeThread;

    private boolean initialized = false;

    private MainWindowController() {

    }

    public static MainWindowController getInstance() {
        if (instance == null)
            instance = new MainWindowController();
        return instance;
    }

    public void setTimeDTO(TimeDTO timeDTO) {
        this.timeDTO = timeDTO;
        Platform.runLater(() -> timeLabel.setText(timeDTO.toString()));
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void onTimeSettingsClick() {
        Utils.openNewWindow("/fxml/timeSettings.fxml", new TimeSettingsController(timeDTO, this), "Ustaw czas", false, false, null);
    }

    public void onOpenSettingsClick() {
        List<LoadingTask> tasks = Arrays
                .asList(SavingPlacesSettingsController.getInstance(),
                        SecurityConfigController.getInstance(),
                        NetworkConfigController.getInstance(),
                        OtherSettingsController.getInstance());
        Utils.openNewWindow("/fxml/settingsLoading.fxml", new HelloController(tasks,
                false, () -> Utils.openNewWindow("/fxml/settingsWindow.fxml", new SettingsWindowController(), "Ustawienia", false, false, (event -> {
            tasks.stream().map(loadingTask -> (Clearable) loadingTask).forEach(Clearable::clear);
        }))), "Ładowanie", true, false, null);
    }

    public void onTakePhotoClick() {
        photoLabel.setVisible(false);
        progress.setVisible(true);
        preview1.setVisible(false);
        preview2.setVisible(false);
        Thread thread = new Thread(() -> {
            ZipInputStream zipInputStream = new ZipInputStream(Utils.getInputStreamFromServer("/api/photo/takePhoto"));
            showPreview(zipInputStream);
        });
        thread.start();
    }

    private void showPreview(ZipInputStream zipInputStream) {
        ZipEntry entity;
        Image image = null;
        Image image2 = null;
        try {
            while ((entity = zipInputStream.getNextEntry()) != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                copyStream(zipInputStream, byteArrayOutputStream);
                zipInputStream.closeEntry();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                switch (entity.getName()) {
                    case "camera1.jpg":
                        image = new Image(byteArrayInputStream);
                        break;
                    case "camera2.jpg":
                        image2 = new Image(byteArrayInputStream);
                        break;
                }
            }
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image finalImage = image;
        Image finalImage1 = image2;
        Platform.runLater(() -> {
            progress.setVisible(false);
            preview1.setImage(finalImage);
            preview2.setImage(finalImage1);
            preview1.setVisible(true);
            preview2.setVisible(true);
        });
        if (ServerStateService.getInstance().getOveralStateDTO().getJpgComputerSaveEnabled()) {
            String separator = null;
            String jpgLocation = ServerStateService.getInstance().getOveralStateDTO().getJpgLocation();
            String system = System.getProperty("os.name");
            if (system.contains("linux") || system.contains("Linux"))
                separator = "/";
            else if (system.contains("Win") || system.contains("win"))
                separator = "\\";
            try {
                String date = LocalDateTime.now().toString();
                ImageIO.write(SwingFXUtils.fromFXImage(preview1.getImage(), null), "jpg", new File(jpgLocation + separator + date.replace(":", "-") + "-camera1.jpg"));
                ImageIO.write(SwingFXUtils.fromFXImage(preview2.getImage(), null), "jpg", new File(jpgLocation + separator + date.replace(":", "-") + "-camera2.jpg"));
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

    public void onUpButtonClick() throws IOException {
        RabbitConnector.getInstance().send("up");
    }

    public void onDownButtonClick() throws IOException {
        RabbitConnector.getInstance().send("down");
    }

    public void onStopButtonClick() throws IOException {
        RabbitConnector.getInstance().send("stop");
    }

    private void copyStream(ZipInputStream inputStream, OutputStream outputStream) throws IOException {
        for (int c = inputStream.read(); c != -1; c = inputStream.read()) {
            outputStream.write(c);
        }
    }

    public void onStartStopButtonClick() {
        ThreadState threadState = timeDTO.getThreadState();
        if (threadState.equals(ThreadState.NEW)) {
            Utils.performActionOnServer("/api/time/start");
            Platform.runLater(() -> timeLabel.setDisable(false));
            timeThread.start();
            startStopButton.setText("Stop");
            timeDTO.setThreadState(ThreadState.RUNNING);
        } else if (threadState.equals(ThreadState.SUSPENDED)) {
            Utils.performActionOnServer("/api/time/start");
            Platform.runLater(() -> timeLabel.setDisable(false));
            timeThread.resume();
            startStopButton.setText("Stop");
            timeDTO.setThreadState(ThreadState.RUNNING);
        } else if (threadState.equals(ThreadState.RUNNING)) {
            Utils.performActionOnServer("/api/time/stop");
            Platform.runLater(() -> timeLabel.setDisable(true));
            timeThread.suspend();
            startStopButton.setText("Start");
            timeDTO.setThreadState(ThreadState.SUSPENDED);
        }
    }

    public void onStartPhoto() {
        timeDTO.reset();
        Platform.runLater(() -> {
            photoLabel.setVisible(false);
            preview1.setVisible(false);
            preview2.setVisible(false);
            progress.setVisible(true);
        });
    }

    public void onPhotoFinished() {
        ZipInputStream zipInputStream = new ZipInputStream(Utils.getInputStreamFromServer("/api/photo/getLastPhotos"));
        showPreview(zipInputStream);
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
            timeDTO = (TimeDTO) Utils.getDTOFromServer("/api/time", TimeDTO.class);
            Platform.runLater(() -> {
                timeProgress.setVisible(false);
                timeLabel.setText(timeDTO.toString());
                timeLabel.setVisible(true);
                timeLabel.setDisable(!timeDTO.getThreadState().equals(ThreadState.RUNNING));
            });
            if (timeDTO.getThreadState().equals(ThreadState.RUNNING)) {
                timeThread.start();
                Platform.runLater(() -> startStopButton.setText("Stop"));
            }
            if(timeDTO.getThreadState().equals(ThreadState.SUSPENDED))
                timeDTO.setThreadState(ThreadState.NEW);
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
                    timeDTO.tick();
                });
            }
        });
        left.pressedProperty().addListener((observableValue, aBoolean, t1) -> {
            try {
                if (t1)
                    RabbitConnector.getInstance().send("left");
                else
                    RabbitConnector.getInstance().send("leftReleased");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        right.pressedProperty().addListener((observableValue, aBoolean, t1) -> {
            try {
                if (t1)
                    RabbitConnector.getInstance().send("right");
                else
                    RabbitConnector.getInstance().send("rightReleased");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        initialized = true;
    }
}
