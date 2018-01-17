package com.raspberry.loading;

import com.raspberry.dto.OveralStateDTO;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.utils.Utils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Klasa odpowiedzialna za połączenie z robotem
 */
public class RobotConnection implements LoadingTask {

    private static RobotConnection instance;

    private volatile boolean finished = false;

    private RobotConnection() {

    }

    public static RobotConnection getInstance() {
        if (instance == null)
            instance = new RobotConnection();
        return instance;
    }

    @Override
    public boolean shouldBeExecuted() {
        OveralStateDTO overalStateDTO = ServerStateService.getInstance().getOveralStateDTO();
        return !overalStateDTO.getRobotConnected() && overalStateDTO.getConnectRobotEnabled();
    }

    @Override
    public String getTaskName() {
        return "Łączenie z robotem...";
    }

    @Override
    public void execute() {
        if (!Utils.performActionOnServer("/api/robot/connectToRobot")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ostrzeżenie");
                alert.setHeaderText("Łączenie z robotem");
                alert.setContentText("Nie udało się połączyć z robotem. Sprawdź czy robot jest włączony, i adres IP jest wyświetlany na wyświetlaczu, oraz czy robot jest w tej sieci co komputer.\nJeśli adres IP jest wyświetlany, przepisz go do następnego okienka.\nCzy chcesz wpisać nowy adres IP robota?");
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(new ButtonType("Tak"), new ButtonType("Nie"));
                ButtonType response = alert.showAndWait().orElse(new ButtonType(("Nie")));
                if (response.getText().equals("Tak"))
                    Utils.openNewWindow("/fxml/ipAddress.fxml",
                            new RobotIpWindowController(), "Podaj adres ip",
                            false, true, null);
                else finished = true;
            });
        } else finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
