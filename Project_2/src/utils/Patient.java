package utils;

public class Patient {
    public int key;
    private int demand;
    private TimeWindow timeWindow;
    private int careTime;

    private int x_coord;
    private int y_coord;

    public Patient(int key, int x_coord, int y_coord, int demand, int start_time, int end_time, int care_time) {
        this.key = key;
        this.demand = demand;
        this.timeWindow = new TimeWindow(start_time, end_time);
        this.careTime = care_time;

        this.x_coord = x_coord;
        this.y_coord = y_coord;
    }

    public String toString() {
        return "Patient: " + demand + ", " + timeWindow + ", " + careTime;
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

    public int getXCoord() {
        return x_coord;
    }

    public int getYCoord() {
        return y_coord;
    }
}
