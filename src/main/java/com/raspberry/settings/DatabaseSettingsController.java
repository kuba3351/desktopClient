package com.raspberry.settings;

import com.raspberry.DatabaseType;
import com.raspberry.dto.DatabaseConfigDTO;
import com.raspberry.utils.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Kontroler ustawień bazy danych
 */
public class DatabaseSettingsController implements Initializable {

    private static DatabaseSettingsController instance;
    @FXML
    private ChoiceBox<DatabaseType> databaseType;
    @FXML
    private TextField host;
    @FXML
    private TextField port;
    @FXML
    private TextField databaseName;
    @FXML
    private TextField userName;
    @FXML
    private PasswordField password;
    @FXML
    private Label settingsChangedLabel;
    private DatabaseConfigDTO databaseConfigDTO;

    private DatabaseSettingsController() {

    }

    public static DatabaseSettingsController getInstance() {
        if (instance == null)
            instance = new DatabaseSettingsController();
        return instance;
    }

    public Label getSettingsChangedLabel() {
        return settingsChangedLabel;
    }

    public DatabaseConfigDTO getDatabaseConfigDTO() {
        return databaseConfigDTO;
    }

    public void setDatabaseConfigDTO(DatabaseConfigDTO databaseConfigDTO) {
        this.databaseConfigDTO = databaseConfigDTO;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingsChangedLabel.setVisible(false);
        databaseType.getItems().addAll(DatabaseType.POSTGRES, DatabaseType.MYSQL);
        databaseType.setValue(databaseConfigDTO.getDatabaseType());
        databaseType.valueProperty().addListener((observableValue, databaseType, t1) -> {
            databaseConfigDTO.setDatabaseType(t1);
            settingsChanged();
        });
        host.setText(databaseConfigDTO.getHost());
        host.textProperty().addListener((observableValue, eventHandler, t1) -> {
            databaseConfigDTO.setHost(host.getText());
            settingsChanged();
        });
        port.setText(databaseConfigDTO.getPort().toString());
        port.textProperty().addListener((observableValue, eventHandler, t1) -> {
            databaseConfigDTO.setPort(Integer.parseInt(port.getText()));
            settingsChanged();
        });
        databaseName.setText(databaseConfigDTO.getDatabaseName());
        databaseName.textProperty().addListener((observableValue, eventHandler, t1) -> {
            databaseConfigDTO.setDatabaseName(databaseName.getText());
            settingsChanged();
        });
        userName.setText(databaseConfigDTO.getUser());
        userName.textProperty().addListener((observableValue, eventHandler, t1) -> {
            databaseConfigDTO.setUser(userName.getText());
            settingsChanged();
        });
        password.textProperty().addListener((observableValue, eventHandler, t1) -> {
            databaseConfigDTO.setPassword(password.getText());
            settingsChanged();
            password.setPromptText("(puste)");
        });
    }

    public void onThisComputerButtonClick() {
        InputStream inputStream = Utils.getInputStreamFromServer("/api/whatIsMyIp");
        Scanner scanner = new Scanner(inputStream);
        host.setText(scanner.next());
    }

    private void settingsChanged() {
        settingsChangedLabel.setVisible(true);
    }
}
