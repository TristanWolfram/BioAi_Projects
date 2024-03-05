import java.lang.reflect.Array;
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

        ArrayList<SolutionRepresentation> children = crossover(population.get(0), population.get(1), 2);
    }

    public void run() {
        for (int i = 0; i < generations; i++) {
            ArrayList<SolutionRepresentation> newPopulation = new ArrayList<SolutionRepresentation>();

            ArrayList<SolutionRepresentation> parents = getParentsRouletteWheelSelect();

            ArrayList<SolutionRepresentation> children = crossover(parents.get(0), parents.get(1), 2);

            for (SolutionRepresentation child : children) {
                if (Math.random() < mutationRate) {
                    // mutate
                }
            }

            newPopulation.addAll(children);

            newPopulation = sortSolution(newPopulation);
            newPopulation = new ArrayList<>(newPopulation.subList(0, populationSize));
            Collections.shuffle(newPopulation);

            population = newPopulation;

            System.out.println(
                    "Generation " + i + " -> best:" + getBestFitness() + " | average:" + getAverageFitness());
        }
    }

    private ArrayList<SolutionRepresentation> getParentsRouletteWheelSelect() {
        ArrayList<SolutionRepresentation> parents = new ArrayList<SolutionRepresentation>();

        parents.add(selectIndividualByRouletteWheel());
        parents.add(selectIndividualByRouletteWheel());

        // make sure parents are different
        while (parents.get(0).equals(parents.get(1))) {
            parents.set(1, selectIndividualByRouletteWheel());
        }

        return parents;
    }

    private SolutionRepresentation selectIndividualByRouletteWheel() {
        int sumFitness = getSumFitness();
        int random = (int) (Math.random() * sumFitness);
        int runningSum = 0;

        for (SolutionRepresentation solution : population) {
            runningSum += solution.getFitness(travelTimes);
            if (runningSum > random) {
                return solution;
            }
        }

        System.out.println("Error selecting individual by roulette wheel");
        return population.get(population.size() - 1);
    }

    private ArrayList<SolutionRepresentation> crossover(SolutionRepresentation parent1, SolutionRepresentation parent2,
            int number) {
        ArrayList<SolutionRepresentation> children = new ArrayList<SolutionRepresentation>();

        // select random route from both parents
        int randomRoute1 = (int) (Math.random() * parent1.getSolution().size());
        int randomRoute2 = (int) (Math.random() * parent2.getSolution().size());

        // find patients of route 1 in solution 2
        SolutionRepresentation child1 = parent2;
        System.out.println("child1: " + child1);
        ArrayList<Patient> Route1 = parent1.getSolution().get(randomRoute1).getRoute().patients;
        System.out.println("Route1: " + Route1);

        for (Patient patient : Route1) {
            for (Nurse nurse : child1.getSolution()) {
                if (nurse.getRoute().patients.contains(patient)) {
                    // remove patient from route 2 and add it to free patients
                    nurse.getRoute().patients.remove(patient);
                }
            }
        }

        System.out.println("child1: " + child1);

        return children;
    }

    private ArrayList<SolutionRepresentation> sortSolution(ArrayList<SolutionRepresentation> currentPopulation) {
        currentPopulation.sort((solution1, solution2) -> Integer.compare(solution1.getFitness(travelTimes),
                solution2.getFitness(travelTimes)));

        return currentPopulation;
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

    public int getSumFitness() {
        int totalFitness = 0;
        for (SolutionRepresentation solution : population) {
            totalFitness += solution.getFitness(travelTimes);
        }
        return totalFitness;
    }
}
