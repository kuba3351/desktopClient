package com.raspberry.dto;

import com.raspberry.CameraType;

/**
 * Klasa służąca do transferu ustawień rozdzielczości zdjęcia
 */
public class PhotoDTO {

    private int width;
    private int heigth;
    private CameraType cameraType;

    public PhotoDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhotoDTO photoDTO = (PhotoDTO) o;

        if (width != photoDTO.width) return false;
        return heigth == photoDTO.heigth;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + heigth;
        return result;
    }

    public PhotoDTO(int width, int heigth) {
        this.width = width;
        this.heigth = heigth;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeigth() {
        return heigth;
    }

    public void setHeigth(int heigth) {
        this.heigth = heigth;
    }
}
