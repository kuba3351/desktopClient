package com.raspberry;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class DatabaseConnector implements LoadingTask {

    private static DatabaseConnector instance;

    public static DatabaseConnector getInstance() {
        if(instance == null)
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
        if(!Utils.performActionOnServer("/api/connectToDatabase")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Błąd połączenia z bazą");
                alert.setContentText("Zapisywanie zdjęć w bazie danych jest włączone, ale serwer nie mógł się z nią połączyć.\n" +
                        "Skoryguj ustawienia lub wyłącz zapisywanie zdjęć w bazie przed przystąpieniem do robienia zdjęć.");
                alert.showAndWait();
            });
        }
    }
}
