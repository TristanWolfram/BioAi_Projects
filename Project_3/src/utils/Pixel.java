package utils;

import java.util.ArrayList;

public class Pixel {
    private final int key;
    private RGBRepresentation color;
    public ArrayList<Pixel> neighbors;

    public Pixel(int key, RGBRepresentation color) {
        this.key = key;
        this.color = color;
    }

    public int getKey() {
        return key;
    }

    public RGBRepresentation getColor() {
        return color;
    }

    public void setColor(RGBRepresentation color) {
        this.color = color;
    }

    public ArrayList<Pixel> getNeighbors() {
        return neighbors;
    }
}