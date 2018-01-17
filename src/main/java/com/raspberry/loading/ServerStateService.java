package com.raspberry.loading;

import com.raspberry.dto.OveralStateDTO;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Klasa odpowiedzialna za sprawdzenie stanu systemu na serwerze i powiadomienie gdy coś jest nie tak
 */
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
            if (overalStateDTO.getPendriveEnabled() && !overalStateDTO.getPendriveConnected()) {
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
