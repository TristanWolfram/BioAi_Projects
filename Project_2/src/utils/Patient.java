package utils;

import java.util.ArrayList;

public class Patient {
    public int key;
    private int demand;
    private TimeWindow timeWindow;
    private int careTime;

    private Coordinate coordinate;

    private ArrayList<PatientDistancePair> distances;

    public Patient(int key, int x_coord, int y_coord, int demand, int start_time, int end_time, int care_time) {
        this.key = key;
        this.demand = demand;
        this.timeWindow = new TimeWindow(start_time, end_time);
        this.careTime = care_time;

        this.coordinate = new Coordinate(x_coord, y_coord);

        this.distances = null;
    }

    public double distanceTo(Patient other) {
        return Math.sqrt(Math.pow(this.coordinate.getX() - other.coordinate.getX(), 2)
                + Math.pow(this.coordinate.getY() - other.coordinate.getY(), 2));
    }

    public String toString() {
        return "(" + key + ") -> " + timeWindow;
    }

    public int getDemand() {
        return demand;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public int getCareTime() {
        return careTime;
    }

    public int getKey() {
        return key;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setDistances(ArrayList<PatientDistancePair> distances) {
        this.distances = distances;
    }

    public ArrayList<PatientDistancePair> getDistances() {
        return distances;
    }

    public PatientDistancePair getNearestPatient() {
        return distances.get(0);
    }
}
