package utils;

public class Nurse {
    private Route route;
    private int capacity;

    public Nurse(int capacity) {
        this.route = null;
        this.capacity = capacity;
    }

    public int getFitnessOfRoute(double[][] travelMatrix) {
        int fitnessOfRoute = route.getTravelTime(travelMatrix);

        if (route.getTotalDemand() > capacity) {
            fitnessOfRoute += 1000;
        }

        return fitnessOfRoute;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String toString() {
        return "Nurse: -> " + route;
    }
}
