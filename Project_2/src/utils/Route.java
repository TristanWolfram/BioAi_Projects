package utils;

import java.util.ArrayList;

public class Route {
    public ArrayList<Patient> patients;

    public Route(ArrayList<Patient> patients) {
        this.patients = patients;
    }

    public int getTotalDemand() {
        int demand = 0;
        for (Patient patient : patients) {
            demand += patient.getDemand();
        }
        return demand;
    }

    public int getTravelTime(double[][] travelMatrix) {

        if (patients.size() == 0) {
            return 0;
        }

        int timeWindowViolation = 1;
        int travelTime = 0;

        // travel from depot to the first patient
        travelTime += travelMatrix[0][patients.get(0).key];

        for (int i = 0; i < patients.size() - 1; i++) {
            TimeWindow current = patients.get(i).getTimeWindow();
            if (travelTime < current.getStart()) {
                // wait if the nurse is too early
                travelTime += (current.getStart() - travelTime);
            } else if (travelTime > current.getEnd()) {
                // hard violation if the nurse is too late
                timeWindowViolation += 2;
                continue;
            }
            // nurse takes care of the patient
            travelTime += patients.get(i).getCareTime();
            // travel to the next patient
            travelTime += travelMatrix[patients.get(i).key][patients.get(i + 1).key];

            if (travelTime > current.getEnd()) {
                // nurse exceeds the time window
                timeWindowViolation += 1;
            }
        }

        // travel from the last patient to the depot
        travelTime += travelMatrix[patients.get(patients.size() - 1).key][0];

        return travelTime * timeWindowViolation;
    }
}
