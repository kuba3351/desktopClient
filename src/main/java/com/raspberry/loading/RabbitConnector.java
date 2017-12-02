package com.raspberry.loading;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.raspberry.interfaces.LoadingTask;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Klasa odpowiedzialna za połączenie z RabbitMQ
 */
public class RabbitConnector implements LoadingTask {

    private static RabbitConnector instance;

    private volatile boolean finished = false;

    private Channel channel;

    private RabbitConnector() {

    }

    public static RabbitConnector getInstance() {
        if (instance == null)
            instance = new RabbitConnector();
        return instance;
    }

    public void send(String message) throws IOException {
        System.out.println("Sending " + message);
        channel.basicPublish("", "test2", null, message.getBytes());
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
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("test", true, false, false, null);
            channel.queueDeclare("test2", true, false, false, null);
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
