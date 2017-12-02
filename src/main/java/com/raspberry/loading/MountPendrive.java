package com.raspberry.loading;

import com.raspberry.dto.OveralStateDTO;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Klasa odpowiedzialna za montowanie pendrive'a podłączonego do serwera
 */
public class MountPendrive implements LoadingTask {

    private static MountPendrive instance;
    private volatile boolean finished = false;

    private MountPendrive() {

    }

    public static MountPendrive getInstance() {
        if (instance == null)
            instance = new MountPendrive();
        return instance;
    }

    @Override
    public boolean shouldBeExecuted() {
        OveralStateDTO overalStateDTO = ServerStateService.getInstance().getOveralStateDTO();
        return overalStateDTO.getPendriveEnabled() && overalStateDTO.getPendriveConnected() && !overalStateDTO.getPendriveMounted();
    }

    @Override
    public String getTaskName() {
        return "Montowanie pendrive...";
    }

    @Override
    public void execute() {
        if (!Utils.performActionOnServer("/api/mountPendrive")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Montowanie pendrive");
                alert.setContentText("Pendrive został wykryty, ale nie udało się go zamontować.\nMoże to być spowodowane uszkodzeniem pendrive lub nieobsługiwanym systemem plików.\nJeśli wiadomo, że pendrive działa poprawnie, musiało dojść do awarii systemu.\nRozwiąż problem przed przystąpieniem do robienia zdjęć.");
                alert.showAndWait();
                finished = true;
            });
        } else finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
