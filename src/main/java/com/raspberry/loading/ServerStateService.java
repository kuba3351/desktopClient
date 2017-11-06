package com.raspberry.loading;

import com.raspberry.utils.Utils;
import com.raspberry.dto.OveralStateDTO;
import com.raspberry.interfaces.LoadingTask;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ServerStateService implements LoadingTask {

    private static ServerStateService instance;

    private volatile boolean finished = false;

    public static ServerStateService getInstance() {
        if(instance == null)
            instance = new ServerStateService();
        return instance;
    }

    private ServerStateService() {

    }

    private OveralStateDTO overalStateDTO;

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
        if(overalStateDTO.getHotspotEnabled()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Informacja");
                alert.setHeaderText("Tryb hotspota");
                alert.setContentText("Raspberry znajduje się w trybie hotspota.\n" +
                        "Aby wyjść z trybu hotspota i przyłączyć raspberry do istniejącej sieci, otwórz ustawienia.");
                alert.showAndWait();
                finished = true;
            });
        }
        else finished = true;
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
