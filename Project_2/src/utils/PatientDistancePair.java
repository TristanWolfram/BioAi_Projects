package utils;

public class PatientDistancePair {
    private final Patient patient;
    private final double distance;

    public PatientDistancePair(Patient patient, double distance) {
        this.patient = patient;
        this.distance = distance;
    }

    public Patient getPatient() {
        return patient;
    }

    public double getDistance() {
        return distance;
    }

    public String toString() {
        return "Patient: " + patient.key + " Distance: " + distance;
    }

    public int compareTo(PatientDistancePair other) {
        return Double.compare(this.distance, other.distance);
    }
}
