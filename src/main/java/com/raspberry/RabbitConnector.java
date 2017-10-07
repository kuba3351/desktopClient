package com.raspberry;

import com.rabbitmq.client.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitConnector implements LoadingTask {

    private static RabbitConnector instance;

    private volatile boolean finished = false;

    public static RabbitConnector getInstance() {
        if(instance == null)
            instance = new RabbitConnector();
        return instance;
    }

    private RabbitConnector() {

    }

    @Override
    public boolean shouldBeExecuted() {
        return true;
    }

    @Override
    public String getTaskName() {
        return "Łączę z raspberry";
    }

    @Override
    public void execute() {
        ConnectionFactory factory = new ConnectionFactory();
        AutoDiscovery autoDiscovery = AutoDiscovery.getInstance();
        factory.setHost(autoDiscovery.getRaspberryIpAddress());
        factory.setUsername("pi");
        factory.setPassword("raspberry");
        Connection connection;
        Channel channel;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("test", true, false, false, null);
            channel.basicConsume("test", true, new EventListener());
            finished = true;
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Błąd");
                alert.setHeaderText("Błąd kolejki eventów");
                alert.setContentText("Połączenie z kolejką eventów nie powiodło się. Nie wszystko będzie działać prawidłowo.");
                alert.showAndWait();
                finished = true;
            });
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
