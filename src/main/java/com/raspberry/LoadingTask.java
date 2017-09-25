package com.raspberry;

public interface LoadingTask {
    boolean shouldBeExecuted();
    String getTaskName();
    void execute();
    boolean isFinished();
}
