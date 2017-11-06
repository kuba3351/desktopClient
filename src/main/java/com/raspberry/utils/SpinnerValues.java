package com.raspberry.utils;

import javafx.scene.control.SpinnerValueFactory;

public class SpinnerValues extends SpinnerValueFactory<Integer> {

    private Integer max;

    private Integer min;

    public SpinnerValues(Integer min, Integer max) {
        this.max = max;
        this.min = min;
    }

    @Override
    public void decrement(int i) {
        if ((int) this.getValue() > min)
            this.setValue((int) this.getValue() - 1);
    }

    @Override
    public void increment(int i) {
        if ((int) this.getValue() < max) {
            this.setValue((int) this.getValue() + 1);
        }
    }
}