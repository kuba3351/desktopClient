package com.raspberry;

public enum CameraType {
    RASPBERRY, USB;

    @Override
    public String toString() {
        switch(this) {
            case USB:
                return "Kamery USB";
            case RASPBERRY:
                return "Kamery Raspberry";
            default: throw new RuntimeException();
        }
    }
}
