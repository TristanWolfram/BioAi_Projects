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

    public double getFitness() {
        double fitness = 0;
        for (Nurse route : solution) {
            fitness += route.getFitnessOfRoute();
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
            nurse.getRoute().sortByTimeBalanced();
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

    public SolutionRepresentation copy() {
        ArrayList<Nurse> clonedNurses = new ArrayList<Nurse>();
        for (Nurse nurse : solution) {
            clonedNurses.add(nurse.copy());
        }
        return new SolutionRepresentation(clonedNurses);
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
        str += "Fitness: " + getFitness() + "\n";
        return str;
    }

    public String exportToStringFormat() {
        String str = "[";
        for (int i = 0; i < solution.size(); i++) {
            str += solution.get(i).exportToStringFormat();
            if (i != solution.size() - 1)
                str += ",\n";
        }
        str += "]";
        return str;
    }
}