package com.raspberry;

import com.raspberry.loading.*;
import com.raspberry.utils.Utils;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        Utils.openNewWindow("/fxml/hello.fxml", new HelloController(Arrays
                        .asList(AutoDiscoveryRunner.getInstance(),
                                RabbitConnector.getInstance(),
                                ServerStateService.getInstance(),
                                SecurityService.getInstance(),
                                SecurityService.getInstance(),
                                RobotConnection.getInstance(),
                                MountPendrive.getInstance(),
                                DatabaseConnector.getInstance()), true, () -> Utils.openNewWindow("/fxml/mainWindow.fxml", MainWindowController.getInstance(), "Okno główne", false, false, null)),
                "Ładowanie...", true, true, null);
    }
}
