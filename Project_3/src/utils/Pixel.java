package utils;

import java.util.ArrayList;

public class Pixel {
    private final int key;

    public PossibleConnections connection;
    public ArrayList<Pixel> neighbors;
    public boolean assigned = false;

    private RGBRepresentation color;
    private double distance;

    public Pixel(int key, RGBRepresentation color) {
        this.key = key;
        this.color = color;
        this.connection = PossibleConnections.NONE;
        this.distance = 0;
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

    public void setConnection(PossibleConnections connection) {
        this.connection = connection;
        calculateDistance();
    }

    public Pixel getConnectedNeighbor() {
        // get the index of the current connection
        if (connection == PossibleConnections.NONE) {
            return null;
        }
        int index = connection.ordinal();
        return neighbors.get(index);
    }

    private void calculateDistance() {

        if (connection == PossibleConnections.NONE || getConnectedNeighbor() == null) {
            this.distance = 0;
        } else {
            RGBRepresentation otherColor = getConnectedNeighbor().getColor();
            this.distance = Math.sqrt(Math.pow(color.getRed() - otherColor.getRed(), 2)
                    + Math.pow(color.getGreen() - otherColor.getGreen(), 2)
                    + Math.pow(color.getBlue() - otherColor.getBlue(), 2));
        }
    }

    public double getDistance() {
        return distance;
    }

    public String toString() {
        return key + "[" + getConnectionString() + "]";
    }

    private String getConnectionString() {
        switch (connection) {
            case RIGHT:
                return "→";
            case LEFT:
                return "←";
            case UP:
                return "↑";
            case DOWN:
                return "↓";
            case UP_RIGHT:
                return "↗";
            case DOWN_RIGHT:
                return "↘";
            case UP_LEFT:
                return "↖";
            case DOWN_LEFT:
                return "↙";
            default:
                return "*";
        }
    }
}