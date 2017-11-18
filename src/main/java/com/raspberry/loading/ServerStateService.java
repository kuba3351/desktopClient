package com.raspberry.loading;

import com.raspberry.dto.OveralStateDTO;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ServerStateService implements LoadingTask {

    private static ServerStateService instance;

    private volatile boolean finished = false;
    private OveralStateDTO overalStateDTO;

    private ServerStateService() {

    }

    public static ServerStateService getInstance() {
        if (instance == null)
            instance = new ServerStateService();
        return instance;
    }

    public OveralStateDTO getOveralStateDTO() {
        return overalStateDTO;
    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }

    @Override
    public String getTaskName() {
        return "Odczytuję stan serwera...";
    }

    @Override
    public void execute() {
        refreshOverallState();
        Platform.runLater(() -> {
            if(overalStateDTO.getHotspotEnabled()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Informacja");
                alert.setHeaderText("Tryb hotspota");
                alert.setContentText("Raspberry znajduje się w trybie hotspota.\n" +
                        "Aby wyjść z trybu hotspota i przyłączyć raspberry do istniejącej sieci, otwórz ustawienia.");
                alert.showAndWait();
            }
            if(overalStateDTO.getCameras() == 1) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Detekcja podłączonych kamer");
                alert.setContentText("Wykryto tylko jedną podłączoną kamerę.\nJeśli nie podłączyłeś drugiej kamery, możesz zignorować ostrzeżenie.\n Jeśli podłączyłeś dwie kamery, może to oznaczać uszkodzenie jednej z kamer lub awarię systemu.");
                alert.showAndWait();
            }
            if(overalStateDTO.getCameras() == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Detekcja podłączonych kamer");
                alert.setContentText("Nie wykryto żadnych podłączonych kamer. Funkcja robienia zdjęć może nie działać.");
                alert.showAndWait();
            }
            if(overalStateDTO.getPendriveEnabled() && !overalStateDTO.getPendriveConnected()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Sprawdzanie pendrive");
                alert.setContentText("Zapis na pendrive jest włączony, ale pendrive nie został wykryty.\nSprawdź, czy pendrive jest podłączony. Jeśli tak, może to oznaczać awarię pendrive lub systemu.\nRozwiąż problem przed przystąpieniem do robienia zdjęć.");
                alert.showAndWait();
            }
            finished = true;
        });
    }

    public void refreshOverallState() {
        overalStateDTO = (OveralStateDTO) Utils.getDTOFromServer("/getAppStatus",
                OveralStateDTO.class);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
