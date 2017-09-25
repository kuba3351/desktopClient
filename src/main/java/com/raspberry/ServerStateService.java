package com.raspberry;

import javafx.scene.control.Alert;
import jdk.internal.util.xml.impl.ReaderUTF8;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

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
        overalStateDTO = (OveralStateDTO)Utils.getDTOFromServer("/getAppStatus",
                OveralStateDTO.class);
        if(overalStateDTO.getHotspotEnabled()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informacja");
            alert.setHeaderText("Tryb hotspota");
            alert.setContentText("Raspberry znajduje się w trybie hotspota.\n" +
                    "Aby wyjść z trybu hotspota i przyłączyć raspberry do istniejącej sieci, otwórz ustawienia.");
            alert.showAndWait();
        }
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
