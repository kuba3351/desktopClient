package com.raspberry.loading;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.raspberry.MainWindowController;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;

/**
 * Klasa odpowiedzialna za odbiór komunikatów z serwera za pomocą RabbitMQ
 */
public class EventListener implements Consumer {
    @Override
    public void handleConsumeOk(String s) {

    }

    @Override
    public void handleCancelOk(String s) {

    }

    @Override
    public void handleCancel(String s) throws IOException {

    }

    @Override
    public void handleShutdownSignal(String s, ShutdownSignalException e) {

    }

    @Override
    public void handleRecoverOk(String s) {

    }

    @Override
    public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
        MainWindowController controller = MainWindowController.getInstance();
        if (controller.isInitialized()) {
            String string = new String(bytes);
            if (string.startsWith("[ERROR]")) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Błąd");
                    alert.setHeaderText("Błąd zapisu danych");
                    alert.setContentText(string.substring(7));
                    alert.show();
                });
            }
            if (string.equals("Taking photo..."))
                controller.onStartPhoto();
            if (string.equals("Photo taken!"))
                controller.onPhotoFinished();
        }
    }
}
