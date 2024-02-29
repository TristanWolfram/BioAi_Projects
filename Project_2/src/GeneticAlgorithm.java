import java.util.ArrayList;
import java.util.Collections;

import utils.*;

public class GeneticAlgorithm {
    int generations;
    int populationSize;
    double mutationRate;
    double crossoverRate;
    double[][] travelTimes;

    ArrayList<SolutionRepresentation> population = new ArrayList<SolutionRepresentation>();

    public GeneticAlgorithm(int generations, int populationSize, double mutationRate, double crossoverRate,
            int nbrNurses, int capacityNurse, ArrayList<Patient> patients, double[][] travelTimes) {
        System.out.println("Initializing Genetic Algorithm...");
        this.generations = generations;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.travelTimes = travelTimes;

        // normally you should check if patients.size() % nbrNurses == 0
        // but the given json files always have 100 patients and 25 nurses
        // -> this will always be 4
        int patientsPerNurse = patients.size() / nbrNurses; // 4

        // for each individual in the population
        for (int i = 0; i < populationSize; i++) {
            Collections.shuffle(patients);
            int patienIndex = 0;
            ArrayList<Nurse> individual = new ArrayList<Nurse>();

            // create 25 nurses
            for (int j = 0; j < nbrNurses; j++) {
                Nurse nurse = new Nurse(capacityNurse);

                // with 4 patients each
                ArrayList<Patient> routePatients = new ArrayList<Patient>();
                for (int k = 0; k < patientsPerNurse; k++) {
                    routePatients.add(patients.get(patienIndex));
                    patienIndex++;
                }

                nurse.setRoute(new Route(routePatients));

                individual.add(nurse);
            }

            population.add(new SolutionRepresentation(individual));
        }

        System.out.println("Genetic Algorithm initialized!");
        System.out.println("Initial fitness -> best:" + getBestFitness() + " | average:" + getAverageFitness());
    }

    public int getBestFitness() {
        int bestFitness = Integer.MAX_VALUE;
        for (SolutionRepresentation solution : population) {
            int fitness = solution.getFitness(travelTimes);
            if (fitness < bestFitness) {
                bestFitness = fitness;
            }
        }
        return bestFitness;
    }

    public int getAverageFitness() {
        int totalFitness = 0;
        for (SolutionRepresentation solution : population) {
            totalFitness += solution.getFitness(travelTimes);
        }
        return totalFitness / populationSize;
    }

}
