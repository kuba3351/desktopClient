package com.raspberry.loading;

import com.raspberry.dto.UsernameAndPasswordDTO;
import com.raspberry.interfaces.LoadingTask;
import com.raspberry.settings.UsernameAndPasswordController;
import com.raspberry.utils.Utils;
import javafx.application.Platform;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Klasa odpowiedzialna za obsługę zabezpieczenia API
 */
public class SecurityService implements LoadingTask {

    private static SecurityService instance;
    private UsernameAndPasswordDTO usernameAndPasswordDTO;
    private volatile boolean finished = false;

    private String token;

    private SecurityService() {

    }

    public static SecurityService getInstance() {
        if (instance == null)
            instance = new SecurityService();
        return instance;
    }

    public String getToken() {
        return token;
    }

    public UsernameAndPasswordDTO getUsernameAndPasswordDTO() {
        return usernameAndPasswordDTO;
    }

    public void setUsernameAndPasswordDTO(UsernameAndPasswordDTO usernameAndPasswordDTO) {
        this.usernameAndPasswordDTO = usernameAndPasswordDTO;
    }

    @Override
    public boolean shouldBeExecuted() {
        return ServerStateService.getInstance()
                .getOveralStateDTO().getSecurityEnabled();
    }

    @Override
    public String getTaskName() {
        return "Konfigurowanie zabezpieczeń";
    }

    @Override
    public void execute() {
        if (usernameAndPasswordDTO == null) {
            Platform.runLater(() -> Utils.openNewWindow("/fxml/UsernameAndPassword.fxml", new UsernameAndPasswordController(), "Wpisz dane logowania", false, true, null));
        } else {
            refreshToken();
            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(1);
            scheduler
                    .scheduleAtFixedRate(this::refreshToken, 150, 150, TimeUnit.SECONDS);
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void refreshToken() {
        token = Utils.refreshToken(usernameAndPasswordDTO);
    }
}
