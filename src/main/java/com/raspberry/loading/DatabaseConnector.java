package com.raspberry.loading;

import com.raspberry.dto.OveralStateDTO;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Klasa odpowiedzialna za połączenie z bazą danych
 */
public class DatabaseConnector implements LoadingTask {

    private static DatabaseConnector instance;

    private volatile boolean finished = false;

    private DatabaseConnector() {

    }

    public static DatabaseConnector getInstance() {
        if (instance == null)
            instance = new DatabaseConnector();
        return instance;
    }

    @Override
    public boolean shouldBeExecuted() {
        OveralStateDTO overalStateDTO = ServerStateService.getInstance().getOveralStateDTO();
        return overalStateDTO.getDatabaseEnabled() && !overalStateDTO.getDatabaseConnected();
    }

    @Override
    public String getTaskName() {
        return "Łączenie z bazą danych...";
    }

    @Override
    public void execute() {
        if (!Utils.performActionOnServer("/api/connectToDatabase")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Błąd połączenia z bazą");
                alert.setContentText("Zapisywanie zdjęć w bazie danych jest włączone, ale serwer nie mógł się z nią połączyć.\n" +
                        "Skoryguj ustawienia lub wyłącz zapisywanie zdjęć w bazie przed przystąpieniem do robienia zdjęć.");
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
