package com.raspberry.dto;

/**
 * Klasa służąca do transferu ustawień rozdzielczości zdjęcia
 */
public class PhotoResolutionDTO {
    private int width;
    private int heigth;

    public PhotoResolutionDTO() {

    }

    public PhotoResolutionDTO(int width, int heigth) {
        this.width = width;
        this.heigth = heigth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhotoResolutionDTO that = (PhotoResolutionDTO) o;

        if (width != that.width) return false;
        return heigth == that.heigth;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + heigth;
        return result;
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
