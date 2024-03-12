package utils.KMeans;

import java.util.ArrayList;

import utils.Coordinate;
import utils.Patient;

public class Cluster {
    private Coordinate centroid;
    private ArrayList<Patient> patients;

    public Cluster(Coordinate centroid) {
        this.centroid = centroid;
        this.patients = new ArrayList<Patient>();
    }

    public Coordinate getCentroid() {
        return centroid;
    }

    public void setCentroid(Coordinate centroid) {
        this.centroid = centroid;
    }

    public ArrayList<Patient> getPatients() {
        return patients;
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    public void clear() {
        patients.clear();
    }

    public boolean updateCentroid(double threshold) {
        if (patients.isEmpty()) {
            return false;
        }

        Coordinate newCentroid = new Coordinate(0, 0);
        for (Patient patient : patients) {
            newCentroid.setX(newCentroid.getX() + patient.getCoordinate().getX());
            newCentroid.setY(newCentroid.getY() + patient.getCoordinate().getY());
        }

        newCentroid.setX(newCentroid.getX() / patients.size());
        newCentroid.setY(newCentroid.getY() / patients.size());

        if (KMeans.calculateDistance(newCentroid, centroid) < threshold) {
            return false;
        }

        centroid = newCentroid;
        return true;
    }
}
