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

        for (int i = 0; i < patients.size(); i++) {
            TimeWindow current = patients.get(i).getTimeWindow();
            if (totalTime < current.getStart()) {
                // wait if the nurse is too early
                totalTime += (current.getStart() - totalTime);
            } else if (totalTime > current.getEnd()) {
                // hard violation if the nurse is too late
                timeWindowViolation += 2;
            }
            // nurse takes care of the patient
            totalTime += patients.get(i).getCareTime();
            // travel to the next patient

            if (totalTime > current.getEnd()) {
                // nurse exceeds the time window
                timeWindowViolation += 1;
            }

            if (i < patients.size() - 1) {
                totalTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
                travelTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
            }
        }

        // travel from the last patient to the depot
        totalTime += travelMatrix[patients.get(patients.size() - 1).key][0];
        travelTime += travelMatrix[patients.get(patients.size() - 1).key][0];

        if (totalTime > returnTime) {
            // hard violation if the nurse is too late at the depot
            timeWindowViolation += 2;
        }

        if (timeWindowViolation > 1) {
            return travelTime * timeWindowViolation * 2;
        } else {
            return travelTime;
        }
    }

    public boolean isFeasible() {

        boolean feasible = true;

        if (patients.size() == 0) {
            return feasible;
        }

        double totalTime = 0;
        totalTime += travelMatrix[0][patients.get(0).key];

        for (int i = 0; i < patients.size(); i++) {
            TimeWindow current = patients.get(i).getTimeWindow();
            if (totalTime < current.getStart()) {
                // wait if the nurse is too early
                totalTime += (current.getStart() - totalTime);
            } else if (totalTime > current.getEnd()) {
                // hard violation if the nurse is too late
                feasible = false;
            }
            // nurse takes care of the patient
            totalTime += patients.get(i).getCareTime();
            // travel to the next patient

            if (totalTime > current.getEnd()) {
                // nurse exceeds the time window
                feasible = false;
            }

            if (i < patients.size() - 1) {
                totalTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];
            }
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

    public void sortRouteEndTime() {
        patients.sort((p1, p2) -> p1.getTimeWindow().getEnd() - p2.getTimeWindow().getEnd());
    }

    public void sortByTimeBalanced() {
        patients.sort((p1, p2) -> p1.getTimeWindow().getMiddleTime() - p2.getTimeWindow().getMiddleTime());
    }

    public Route copy() {
        ArrayList<Patient> clonedPatients = new ArrayList<Patient>();
        for (Patient patient : patients) {
            clonedPatients.add(patient);
        }
        return new Route(clonedPatients, travelMatrix, returnTime);
    }

    public String toString() {
        return "Route(" + getTravelTime() + "):\t" + patients + "\n\tTotal demand: ---> "
                + getTotalDemand()
                + ",\tIs the route feasible: ---> "
                + isFeasible();
    }

    public String exportToStringFormat() {
        String str = "[";
        for (int i = 0; i < patients.size(); i++) {
            str += patients.get(i).getKey();
            if (i < patients.size() - 1) {
                str += ", ";
            }
        }
        str += "]";
        return str;
    }
}
