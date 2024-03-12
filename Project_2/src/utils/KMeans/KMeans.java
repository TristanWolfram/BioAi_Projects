package utils.KMeans;

import java.util.ArrayList;

import utils.Coordinate;
import utils.Patient;

public class KMeans {

    public static ArrayList<Cluster> kMeans(ArrayList<Patient> patients, int k, double threshold) {
        ArrayList<Cluster> clusters = initializeClusters(patients, k);
        boolean finished = false;

        while (!finished) {
            for (Patient patient : patients) {
                Cluster closest = findClosestCluster(clusters, patient);
                closest.addPatient(patient);
            }

            finished = true;
            for (Cluster cluster : clusters) {
                if (cluster.updateCentroid(threshold)) {
                    finished = false;
                }
            }

            if (!finished) {
                clearClusters(clusters);
            }
        }

        return clusters;
    }

    private static ArrayList<Cluster> initializeClusters(ArrayList<Patient> patients, int k) {
        ArrayList<Cluster> clusters = new ArrayList<>();
        ArrayList<Patient> usedPatients = new ArrayList<>();

        while (usedPatients.size() < k) {
            Patient selected = selectRandomPatient(patients);
            if (!usedPatients.contains(selected)) {
                usedPatients.add(selected);
            }
        }

        for (Patient patient : usedPatients) {
            Cluster newCluster = new Cluster(patient.getCoordinate());
            clusters.add(newCluster);
        }

        return clusters;
    }

    private static Patient selectRandomPatient(ArrayList<Patient> patients) {
        return patients.get((int) (Math.random() * patients.size()));
    }

    private static Cluster findClosestCluster(ArrayList<Cluster> clusters, Patient patient) {

        Cluster nearesCluster = null;

        for (Cluster cluster : clusters) {
            if (nearesCluster == null) {
                nearesCluster = cluster;
            } else {
                if (calculateDistance(cluster.getCentroid(), patient.getCoordinate()) < calculateDistance(
                        nearesCluster.getCentroid(), patient.getCoordinate())) {
                    nearesCluster = cluster;
                }
            }
        }

        return nearesCluster;

    }

    private static void clearClusters(ArrayList<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            cluster.clear();
        }
    }

    public static double calculateDistance(Coordinate a, Coordinate b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }
}
