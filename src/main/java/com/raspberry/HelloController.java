package com.raspberry;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable{

    @FXML
    private ImageView splashImage;

    @FXML
    private ProgressBar loadingProgress;

    @FXML
    private Label taskLabel;

    private List<LoadingTask> tasks;

    private boolean haveSplashImage;

    private CallbackInterface callback;

    public HelloController(List<LoadingTask> tasks, boolean haveSplashImage, CallbackInterface callback) {
        this.tasks = tasks;
        this.haveSplashImage = haveSplashImage;
        this.callback = callback;
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(haveSplashImage) {
            splashImage.setImage(new Image(HelloController.class
                    .getResourceAsStream("/images/raspberry.jpg")));
        }
        Thread loading = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tasks.forEach(loadingTask -> {
                if(loadingTask.shouldBeExecuted()) {
                    Platform.runLater(() -> {
                        taskLabel.setText(loadingTask.getTaskName());
                        loadingProgress.setProgress(((double)tasks.indexOf(loadingTask) + 1d) / (double)tasks.size());
                    });
                    loadingTask.execute();
                    while (!loadingTask.isFinished()) {

                    }
                }
            });
            Platform.runLater(() -> {
                ((Stage)taskLabel.getScene().getWindow()).close();
                callback.callback();
            });
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        loading.start();
    }
}
