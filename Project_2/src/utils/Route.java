package utils;

public class Route {
    public Patient[] patients;

    public Route(Patient[] patients) {
        this.patients = patients;
    }

    public float getTravelTime() {
        return 0.0f;
    }
}
