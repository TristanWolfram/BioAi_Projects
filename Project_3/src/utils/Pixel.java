package utils;

import java.util.ArrayList;

public class Pixel {
    private final int key;

    public PossibleConnections connection;
    public ArrayList<Integer> neighbors;
    public boolean assigned = false;

    private RGBRepresentation color;

    public Pixel(int key, RGBRepresentation color) {
        this.key = key;
        this.color = color;
        this.connection = PossibleConnections.NONE;
    }

    public Pixel(int key, RGBRepresentation color, ArrayList<Integer> neighbours) {
        this.key = key;
        this.color = color;
        this.connection = PossibleConnections.NONE;
        this.neighbors = neighbours;
    }

    //make a copy but a new element
    public Pixel(Pixel pixel) {
        this.key = pixel.key;
        this.connection = pixel.connection;
        this.neighbors = new ArrayList<Integer>(pixel.neighbors);
        this.assigned = pixel.assigned;
        this.color = pixel.color;
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

    public ArrayList<Integer> getNeighbors() {
        return neighbors;
    }

    public void setConnection(PossibleConnections connection) {
        this.connection = connection;
    }

    public Integer getConnectedNeighbor() {
        // get the index of the current connection
        if (connection == PossibleConnections.NONE) {
            return -1;
        }
        int index = connection.ordinal();
        return neighbors.get(index);
    }

    public double getDistanceTo(RGBRepresentation otherColor){
            double distance= Math.sqrt(Math.pow(color.getRed() - otherColor.getRed(), 2)
                    + Math.pow(color.getGreen() - otherColor.getGreen(), 2)
                    + Math.pow(color.getBlue() - otherColor.getBlue(), 2));
        return distance;
    }


    public String toString() {
        if (key < 10){
            return key + " [" + getConnectionString() + "]";
        }
        return key + "[" + getConnectionString() + "]";
    }

    private String getConnectionString() {
        switch (connection) {
            case RIGHT:
                return "1";
            case LEFT:
                return "2";
            case UP:
                return "3";
            case DOWN:
                return "4";
            case UP_RIGHT:
                return "5";
            case DOWN_RIGHT:
                return "6";
            case UP_LEFT:
                return "7";
            case DOWN_LEFT:
                return "8";
            default:
                return "*";
        }
    }
}