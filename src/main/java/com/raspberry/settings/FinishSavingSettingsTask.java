package com.raspberry.settings;

import com.raspberry.interfaces.LoadingTask;
import com.raspberry.loading.ServerStateService;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Klasa odpowiedzialna za kończenie zapisu ustawień na serwerze
 */
public class FinishSavingSettingsTask implements LoadingTask {

    private static FinishSavingSettingsTask instance;
    private boolean finished = false;

    private FinishSavingSettingsTask() {

    }

    public static FinishSavingSettingsTask getInstance() {
        if (instance == null)
            instance = new FinishSavingSettingsTask();
        return instance;
    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }

    @Override
    public String getTaskName() {
        return "Kończenie zapisu ustawień...";
    }

    @Override
    public void execute() {
        ServerStateService.getInstance().refreshOverallState();
        if (SecurityConfigController.getInstance().areSettingsChanged() || NetworkConfigController.getInstance().areSettingsChanged() || OtherSettingsController.getInstance().isImportantSettingsChanged()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Zmiana ważnych ustawień");
                alert.setContentText("Dokoano zmiany ważnych ustawień serwera. \nAby weszły onew życie wymagane jest jego ponowne uruchomienie. \nJeśli zmienieś ustawienia sieciowe, przepnij ten komputer do nowej sieci. \nJeśli połączenie z nową siecią się nie powiedzie, ustawienia sieci wrócą do poprzednich.\n Ten program zostanie teraz zamknięty. Uruchom go ponownie gdy serwer się uruchomi.");
                alert.showAndWait();
                Utils.performActionOnServer("/rebootServer");
                System.exit(0);
            });
        } else finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
