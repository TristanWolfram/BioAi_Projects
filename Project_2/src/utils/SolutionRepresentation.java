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

    public int getFitness(double[][] travelMatrix) {
        int fitness = 0;
        for (Nurse route : solution) {
            fitness += route.getFitnessOfRoute(travelMatrix);
        }
        return fitness;
    }
}