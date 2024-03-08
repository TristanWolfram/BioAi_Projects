package utils;

import java.util.ArrayList;

public class SolutionRepresentation {
    ArrayList<Nurse> solution;

    public SolutionRepresentation(ArrayList<Nurse> solution) {
        this.solution = solution;
    }

    public ArrayList<Nurse> getSolution() {
        return solution;
    }

    public double getFitness(double[][] travelMatrix, int return_time) {
        double fitness = 0;
        for (Nurse route : solution) {
            fitness += route.getFitnessOfRoute(travelMatrix, return_time);
        }
        return fitness;
    }

    public int getPatientCount() {
        int patientCount = 0;
        for (Nurse route : solution) {
            patientCount += route.getPatientCount();
        }
        return patientCount;
    }

    public void sortPatients() {
        for (Nurse nurse : solution) {
            nurse.getRoute().sortRouteStartTime();
        }
    }

    public boolean isFeasible(double[][] travelMatrix, int return_time) {
        boolean feasible = true;
        for (Nurse nurse : solution) {
            if (nurse.getRoute().getTotalDemand() > nurse.getCapacity()) {
                feasible = false;
                return feasible;
            }
            if (!nurse.getRoute().isFeasible()) {
                feasible = false;
                return feasible;
            }
        }
        return feasible;
    }

    public String toString() {
        String str = "Solution:\n";

        for (Nurse nurse : solution) {
            str += nurse.toString() + "\n";
        }

        return str;
    }

    public String toStringSimple(double[][] travelMatrix, int returnTime) {
        String str = "Solution:\n";
        str += "Fitness: " + getFitness(travelMatrix, returnTime) + "\n";
        return str;
    }
}