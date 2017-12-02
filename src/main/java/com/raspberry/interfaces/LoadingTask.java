package com.raspberry.interfaces;

public interface LoadingTask {
    boolean shouldBeExecuted();

    String getTaskName();

    void execute();

    boolean isFinished();
}
