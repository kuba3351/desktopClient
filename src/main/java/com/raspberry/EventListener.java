package com.raspberry;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;

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
        String string = new String(bytes);
        MainWindowController controller = MainWindowController.getInstance();
        if(controller.isInitialized()) {
            if (string.equals("Taking photo..."))
                controller.onStartPhoto();
            else if (string.equals("Photo taken!"))
                controller.onPhotoFinished();
        }
    }
}
