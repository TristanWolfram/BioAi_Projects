package utils;

public class Nurse {
    private Route route;
    private int capacity;

    public Nurse(int capacity) {
        this.route = null;
        this.capacity = capacity;
    }

    public double getFitnessOfRoute() {
        double fitnessOfRoute = route.getTravelTime();

        if (route.getTotalDemand() > capacity) {
            fitnessOfRoute += 2000.0;
        }

        return fitnessOfRoute;
    }

    public int getPatientCount() {
        return route.getPatientCount();
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public int getCapacity() {
        return capacity;
    }

    public Nurse copy() {
        Nurse nurse = new Nurse(capacity);
        nurse.setRoute(route.copy());
        return nurse;
    }

    public String toString() {
        return "Nurse: -> " + route;
    }

    public String exportToStringFormat() {
        return route.exportToStringFormat();
    }
}
