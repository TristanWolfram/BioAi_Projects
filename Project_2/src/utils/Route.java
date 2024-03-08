package utils;

import java.util.ArrayList;

public class Route {
    private ArrayList<Patient> patients;
    private double[][] travelMatrix;
    private int returnTime;

    public Route(ArrayList<Patient> patients, double[][] travelMatrix, int returnTime) {
        this.patients = patients;
        this.travelMatrix = travelMatrix;
        this.returnTime = returnTime;
    }

    public int getTotalDemand() {
        int demand = 0;
        for (Patient patient : patients) {
            demand += patient.getDemand();
        }
        return demand;
    }

    public int getPatientCount() {
        return patients.size();
    }

    public ArrayList<Patient> getPatients() {
        return patients;
    }

    public double getTravelTime() {

        if (patients.size() == 0) {
            return 0;
        }

        int timeWindowViolation = 1;
        double totalTime = 0;
        double travelTime = 0;

        // travel from depot to the first patient
        totalTime += travelMatrix[0][patients.get(0).key];
        travelTime += travelMatrix[0][patients.get(0).key];

        for (int i = 0; i < patients.size() - 1; i++) {
            TimeWindow current = patients.get(i).getTimeWindow();
            if (totalTime < current.getStart()) {
                // wait if the nurse is too early
                totalTime += (current.getStart() - totalTime);
            } else if (totalTime > current.getEnd()) {
                // hard violation if the nurse is too late
                timeWindowViolation += 2;
                totalTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
                travelTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
                continue;
            }
            // nurse takes care of the patient
            totalTime += patients.get(i).getCareTime();
            // travel to the next patient

            if (totalTime > current.getEnd()) {
                // nurse exceeds the time window
                timeWindowViolation += 1;
            }

            totalTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
            travelTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
        }

        // travel from the last patient to the depot
        totalTime += travelMatrix[patients.get(patients.size() - 1).key][0];
        travelTime += travelMatrix[patients.get(patients.size() - 1).key][0];

        if (totalTime > returnTime) {
            // hard violation if the nurse is too late at the depot
            timeWindowViolation += 2;
        }

        return travelTime * timeWindowViolation;
    }

    public boolean isFeasible() {

        boolean feasible = true;

        if (patients.size() == 0) {
            return feasible;
        }

        double totalTime = 0;
        totalTime += travelMatrix[0][patients.get(0).key];

        for (int i = 0; i < patients.size() - 1; i++) {
            TimeWindow current = patients.get(i).getTimeWindow();
            if (totalTime < current.getStart()) {
                // wait if the nurse is too early
                totalTime += (current.getStart() - totalTime);
            } else if (totalTime > current.getEnd()) {
                // hard violation if the nurse is too late
                feasible = false;
                totalTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
            }
            // nurse takes care of the patient
            totalTime += patients.get(i).getCareTime();
            // travel to the next patient

            if (totalTime > current.getEnd()) {
                // nurse exceeds the time window
                feasible = false;
            }

            totalTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
        }

        // travel from the last patient to the depot
        totalTime += travelMatrix[patients.get(patients.size() - 1).key][0];

        if (totalTime > returnTime) {
            // hard violation if the nurse is too late at the depot
            feasible = false;
        }

        return feasible;
    }

    public void sortRouteStartTime() {
        patients.sort((p1, p2) -> p1.getTimeWindow().getStart() - p2.getTimeWindow().getStart());
    }

    public String toString() {
        return "Route(" + getTravelTime() + "):\t" + patients + "\n\tTotal demand: ---> "
                + getTotalDemand()
                + ",\tIs the route feasible: ---> "
                + isFeasible();
    }
}
