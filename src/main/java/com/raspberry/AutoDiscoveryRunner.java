package com.raspberry;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public class AutoDiscoveryRunner implements LoadingTask {

    private static AutoDiscoveryRunner instance;

    public static AutoDiscoveryRunner getInstance() {
        if(instance == null)
            instance = new AutoDiscoveryRunner();
        return instance;
    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }

    @Override
    public String getTaskName() {
        return "Szukam raspberry w sieci...";
    }

    @Override
    public void execute() {
        final AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
        Thread thread;
        for(int i = 0;i<3&&autoDiscovery.getRaspberryIpAddress() == null;i++) {
            thread = new Thread(autoDiscovery::findRaspberryIp);
            thread.start();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread.stop();
        }
        if(autoDiscovery.getRaspberryIpAddress() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.getButtonTypes().addAll(new ButtonType("Tak"), new ButtonType("Nie"));
            alert.setTitle("Ostrzeżenie");
            alert.setHeaderText("Nie udało się znaleźć Raspberry");
            alert.setContentText("Poszukiwanie Raspberry w sieci nie powiodło się.\n" +
                    "Sprawdź, czy komputer jest w tej samej sieci co raspberry. \n" +
                    "Przyczyną może być również zablokowany port 9000. Jeśli jesteś pewien, że raspberry jest w tej sieci,\n" +
                    "Możesz ręcznie podać adres ip.\n\n" +
                    "Czy chcesz ręcznie podać adres IP?");
            ButtonType response = alert.showAndWait().orElseGet(() -> {
                System.exit(0);
                return new ButtonType("");
            });
            if(response.getText().equals("Tak"))
                Utils.openNewWindow("/fxml/ipAddress.fxml",
                        new IpWindowController(), "Podaj adres ip",
                        false, true);
            else
                System.exit(0);
        }
    }
}