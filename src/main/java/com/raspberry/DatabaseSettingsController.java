package com.raspberry;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class DatabaseSettingsController implements Initializable {

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

    private DatabaseConfigDTO databaseConfigDTO;

    private static DatabaseSettingsController instance;

    public static DatabaseSettingsController getInstance() {
        if(instance == null)
            instance = new DatabaseSettingsController();
        return instance;
    }

    private DatabaseSettingsController() {

    }

    public DatabaseConfigDTO getDatabaseConfigDTO() {
        return databaseConfigDTO;
    }

    public void setDatabaseConfigDTO(DatabaseConfigDTO databaseConfigDTO) {
        this.databaseConfigDTO = databaseConfigDTO;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseType.getItems().addAll(DatabaseType.POSTGRES, DatabaseType.MYSQL, DatabaseType.ORACLE);
        databaseType.setValue(databaseConfigDTO.getDatabaseType());
        host.setText(databaseConfigDTO.getHost());
        port.setText(databaseConfigDTO.getPort().toString());
        databaseName.setText(databaseConfigDTO.getDatabaseName());
        userName.setText(databaseConfigDTO.getDatabaseName());
    }

    public void onThisComputerButtonClick() {
        InputStream inputStream = Utils.getInputStreamFromServer("/api/whatIsMyIp");
        Scanner scanner = new Scanner(inputStream);
        host.setText(scanner.next());
    }
}
