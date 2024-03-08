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
    int returnTime;

    ArrayList<SolutionRepresentation> population = new ArrayList<SolutionRepresentation>();

    public GeneticAlgorithm(int generations, int populationSize, double mutationRate, double crossoverRate,
            int nbrNurses, int capacityNurse, ArrayList<Patient> patients, double[][] travelTimes, int returnTime) {
        System.out.println("Initializing Genetic Algorithm...");
        this.generations = generations;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.travelTimes = travelTimes;
        this.returnTime = returnTime;

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

                    Patient currentPatient = patients.get(patienIndex);
                    ArrayList<PatientDistancePair> currentDistances = new ArrayList<PatientDistancePair>();

                    // find the nearest 3 patients
                    for (Patient patient : patients) {
                        if (patient.equals(currentPatient)) {
                            continue;
                        }
                        currentDistances.add(new PatientDistancePair(patient, currentPatient.distanceTo(patient)));
                    }
                    currentDistances.sort((pair1, pair2) -> pair1.compareTo(pair2));
                    currentPatient.setNearestThreePatients(new ArrayList<>(currentDistances.subList(0, 3)));
                    routePatients.add(currentPatient);

                    patienIndex++;
                }

                nurse.setRoute(new Route(routePatients, travelTimes, returnTime));

                individual.add(nurse);
            }

            population.add(new SolutionRepresentation(individual));
        }

        System.out.println("Genetic Algorithm initialized!");
        System.out.println("Initial fitness -> best:" + getBestFitness() + " | average:" + getAverageFitness());
        System.out.println("Best soulution is feasible: " + getBestSolution().isFeasible(travelTimes, returnTime));
    }

    public SolutionRepresentation run() {
        for (int i = 0; i < generations; i++) {
            ArrayList<SolutionRepresentation> newPopulation = new ArrayList<SolutionRepresentation>();

            int parentPool = populationSize / 25;
            if (parentPool % 2 != 0) {
                parentPool++;
            }
            ArrayList<SolutionRepresentation> parents = getParentsRouletteWheelSelect(parentPool);
            ArrayList<SolutionRepresentation> children = new ArrayList<SolutionRepresentation>();

            for (int j = 0; j < parentPool; j += 2) {
                if (Math.random() < crossoverRate) {
                    children.addAll(crossover(parents.get(j), parents.get(j + 1), 2));
                }
            }

            for (SolutionRepresentation child : children) {
                mutate(child);
            }

            newPopulation.addAll(children);

            newPopulation = sortSolution(newPopulation);
            newPopulation = new ArrayList<>(population.subList(0, populationSize));
            // Collections.shuffle(newPopulation);

            population = newPopulation;

            if (i % 100 == 0) {
                System.out.println("Generation: " + i + " | best fitness: " + getBestFitness() + " | average fitness: "
                        + getAverageFitness() + " | best solution is feasible: "
                        + getBestSolution().isFeasible(travelTimes, returnTime));
            }
        }

        System.out.println("Genetic Algorithm finished!");
        SolutionRepresentation best = getBestSolution();
        return best;
    }

    private ArrayList<SolutionRepresentation> getParentsRouletteWheelSelect(int sizeParentPool) {
        ArrayList<SolutionRepresentation> parents = new ArrayList<SolutionRepresentation>();

        while (parents.size() < sizeParentPool) {
            SolutionRepresentation selected = selectIndividualByRouletteWheel();
            if (!parents.contains(selected)) {
                parents.add(selected);
            }
        }

        return parents;
    }

    private SolutionRepresentation selectIndividualByRouletteWheel() {
        double sumFitness = getSumFitness();
        double random = Math.random() * sumFitness;
        double runningSum = 0;

        for (SolutionRepresentation solution : population) {
            runningSum += solution.getFitness(travelTimes, returnTime);
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

        for (int i = 0; i < number; i++) {
            // select random route from both parents
            int randomRoute1 = (int) (Math.random() * parent1.getSolution().size());
            int randomRoute2 = (int) (Math.random() * parent2.getSolution().size());

            ArrayList<Patient> Route1 = parent1.getSolution().get(randomRoute1).getRoute().getPatients();
            ArrayList<Patient> Route2 = parent2.getSolution().get(randomRoute2).getRoute().getPatients();

            SolutionRepresentation child1 = new SolutionRepresentation(parent1.getSolution());
            SolutionRepresentation child2 = new SolutionRepresentation(parent2.getSolution());

            // create child 1 out of parent 1
            for (Patient patient : Route2) {
                for (Nurse nurse : child1.getSolution()) {
                    if (nurse.getRoute().getPatients().contains(patient)) {
                        // remove patient from route 1 and add it to free patients
                        nurse.getRoute().getPatients().remove(patient);
                    }
                }
                insertPatientBasedOnNeighbourhood(child1, patient);
            }
            child1.sortPatients();

            // create child 2 out of parent 2
            for (Patient patient : Route1) {
                for (Nurse nurse : child2.getSolution()) {
                    if (nurse.getRoute().getPatients().contains(patient)) {
                        // remove patient from route 2 and add it to free patients
                        nurse.getRoute().getPatients().remove(patient);
                    }
                }
                insertPatientBasedOnNeighbourhood(child2, patient);
            }
            child2.sortPatients();

            assert child1.getPatientCount() == 100;
            assert child2.getPatientCount() == 100;

            // if (child1.getFitness(travelTimes, returnTime) <
            // parent1.getFitness(travelTimes, returnTime)) {
            // children.add(child1);
            // }
            // if (child2.getFitness(travelTimes, returnTime) <
            // parent2.getFitness(travelTimes, returnTime)) {
            // children.add(child2);
            // }
            children.add(child1);
            children.add(child2);
        }

        return children;
    }

    private void insertPatientBasedOnNeighbourhood(SolutionRepresentation solution, Patient patient) {
        // find the best place to insert the patient
        Patient nearestNeighbor = patient.getNearestPatient().getPatient();
        Boolean inserted = false;

        for (Nurse nurse : solution.getSolution()) {
            if (nurse.getRoute().getPatients().contains(nearestNeighbor)) {
                if (nurse.getRoute().getPatients().size() >= 6) {
                    insertPatientBasedOnNurseWithFewestWorkload(solution, patient);
                    inserted = true;
                    return;
                } else {
                    // insert patient next to nearest neighbor
                    insertPatientNextToNeighbor(nurse.getRoute(), patient);
                    inserted = true;
                }
                // insertPatientNextToNeighbor(nurse.getRoute(), patient);
                // inserted = true;
            }
        }

        assert inserted;
    }

    private void insertPatientBasedOnNurseWithFewestWorkload(SolutionRepresentation solution, Patient patient) {
        // find the best place to insert the patient
        Nurse nurseWithViewestPatients = solution.getSolution().get(0);
        for (Nurse nurse : solution.getSolution()) {
            if (nurse.getRoute().getTotalDemand() < nurseWithViewestPatients.getRoute().getTotalDemand()) {
                nurseWithViewestPatients = nurse;
            }
        }

        insertPatientInRoute(nurseWithViewestPatients.getRoute(), patient);
    }

    private void insertPatientInRoute(Route route, Patient patient) {

        if (route.getPatients().size() == 0) {
            route.getPatients().add(patient);
            return;
        }
        // find the best place to insert the patient based on the care time
        int curentPatientStartTime = patient.getTimeWindow().getStart();

        for (int i = 0; i < route.getPatients().size(); i++) {
            if (curentPatientStartTime < route.getPatients().get(i).getTimeWindow().getStart()) {
                route.getPatients().add(i, patient);
                return;
            }
        }

        route.getPatients().add(patient);
    }

    private void insertPatientNextToNeighbor(Route route, Patient patient) {
        // find the best place to insert the patient
        Patient nearestNeighbor = patient.getNearestPatient().getPatient();
        int index = route.getPatients().indexOf(nearestNeighbor);
        int curentPatientStartTime = patient.getTimeWindow().getStart();
        int nearestNeighborStartTime = nearestNeighbor.getTimeWindow().getStart();
        if (curentPatientStartTime < nearestNeighborStartTime) {
            route.getPatients().add(index, patient);
        } else {
            route.getPatients().add(index + 1, patient);
        }
    }

    // mutations
    private void mutate(SolutionRepresentation solution) {
        // swap mutation

        for (Nurse nurse : solution.getSolution()) {
            for (int i = 0; i < nurse.getRoute().getPatients().size(); i++) {
                if (Math.random() < mutationRate) {
                    int mutation = (int) (Math.random() * 2);
                    switch (mutation) {
                        case 0:
                            mutateByReordering(nurse.getRoute(), i);
                            break;
                        case 1:
                            mutateBySwappingNurse(solution, nurse.getRoute().getPatients().remove(i));
                        default:
                            break;
                    }
                }
            }
        }

    }

    private void mutateByReordering(Route route, int idx) {
        Patient patient = route.getPatients().remove(idx);
        insertPatientInRoute(route, patient);
    }

    private void mutateBySwappingNurse(SolutionRepresentation solution, Patient patient) {
        int newNurseIndex = (int) (Math.random() * solution.getSolution().size());
        insertPatientInRoute(solution.getSolution().get(newNurseIndex).getRoute(), patient);
    }

    private ArrayList<SolutionRepresentation> sortSolution(ArrayList<SolutionRepresentation> currentPopulation) {
        currentPopulation.sort((solution1, solution2) -> Double.compare(solution1.getFitness(travelTimes, returnTime),
                solution2.getFitness(travelTimes, returnTime)));

        return currentPopulation;
    }

    public SolutionRepresentation getBestSolution() {
        SolutionRepresentation bestSolution = population.get(0);
        for (SolutionRepresentation solution : population) {
            if (solution.getFitness(travelTimes, returnTime) < bestSolution.getFitness(travelTimes, returnTime)) {
                bestSolution = solution;
            }
        }
        return bestSolution;
    }

    public double getBestFitness() {
        double bestFitness = Integer.MAX_VALUE;
        for (SolutionRepresentation solution : population) {
            double fitness = solution.getFitness(travelTimes, returnTime);
            if (fitness < bestFitness) {
                bestFitness = fitness;
            }
        }
        return bestFitness;
    }

    public double getAverageFitness() {
        double totalFitness = 0;
        for (SolutionRepresentation solution : population) {
            totalFitness += solution.getFitness(travelTimes, returnTime);
        }
        return totalFitness / populationSize;
    }

    public double getSumFitness() {
        double totalFitness = 0;
        for (SolutionRepresentation solution : population) {
            totalFitness += solution.getFitness(travelTimes, returnTime);
        }
        return totalFitness;
    }
}
