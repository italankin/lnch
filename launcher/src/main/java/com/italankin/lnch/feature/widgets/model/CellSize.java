package com.italankin.lnch.feature.widgets.model;

public class CellSize {

    public final int width;
    public final int height;
    public final int widthCells;
    public final int heightCells;

    public CellSize(int width, int height, int widthCells, int heightCells) {
        if (width < 0 || height < 0 || widthCells < 0 || heightCells < 0) {
            throw new IllegalArgumentException("Invalid size: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.widthCells = widthCells;
        this.heightCells = heightCells;
    }

    public int maxAvailableHeight() {
        return height * heightCells;
    }

    public int maxAvailableWidth() {
        return width * widthCells;
    }

    public boolean isEmpty() {
        return width == 0 || height == 0;
    }
}
