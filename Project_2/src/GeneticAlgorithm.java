import java.util.ArrayList;
import java.util.Collections;

import utils.*;
import utils.KMeans.*;

public class GeneticAlgorithm {
    int generations;
    int populationSize;
    double mutationRate;
    double crossoverRate;
    int startingClusters;
    int nbrNurses;
    int capacityNurse;
    ArrayList<Patient> patients;
    double[][] travelTimes;
    int returnTime;

    ArrayList<SolutionRepresentation> population = new ArrayList<SolutionRepresentation>();

    public GeneticAlgorithm(int generations, int populationSize, double mutationRate, double crossoverRate,
            int startingClusters,
            int nbrNurses, int capacityNurse, ArrayList<Patient> patients, double[][] travelTimes, int returnTime) {
        System.out.println("Initializing Genetic Algorithm...");
        this.generations = generations;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.startingClusters = startingClusters;
        this.nbrNurses = nbrNurses;
        this.capacityNurse = capacityNurse;
        this.patients = patients;
        this.travelTimes = travelTimes;
        this.returnTime = returnTime;

        // for each individual in the population
        for (int i = 0; i < populationSize; i++) {
            Collections.shuffle(patients);

            // create clusters
            SolutionRepresentation newInitialSolution = createSolutionRepresentationKMeans();
            population.add(newInitialSolution);
        }

        System.out.println("Genetic Algorithm initialized!");
        System.out.println("Initial fitness -> best:" + getBestFitness() + " | average:" + getAverageFitness());
        System.out.println("Best soulution is feasible: " + getBestSolution().isFeasible(travelTimes, returnTime));
    }

    private SolutionRepresentation createSolutionRepresentationKMeans() {
        // create clusters
        ArrayList<Cluster> clusters = KMeans.kMeans(patients, startingClusters, 2);
        int numClusters = clusters.size();
        int numRemainingNurses = nbrNurses - numClusters;

        ArrayList<Nurse> individual = new ArrayList<Nurse>();

        // create nurses
        for (int j = 0; j < numClusters; j++) {
            Nurse nurse = new Nurse(capacityNurse);
            ArrayList<Patient> routePatients = clusters.get(j).getPatients();
            nurse.setRoute(new Route(routePatients, travelTimes, returnTime));
            individual.add(nurse);
        }
        for (int j = 0; j < numRemainingNurses; j++) {
            Nurse nurse = new Nurse(capacityNurse);
            nurse.setRoute(new Route(new ArrayList<Patient>(), travelTimes, returnTime));
            individual.add(nurse);
        }

        SolutionRepresentation newInitialSolution = new SolutionRepresentation(individual);
        newInitialSolution.sortPatients();

        return newInitialSolution;
    }

    public SolutionRepresentation run() {
        for (int i = 0; i < generations; i++) {
            ArrayList<SolutionRepresentation> newPopulation = new ArrayList<SolutionRepresentation>(population);

            int parentPool = populationSize / 10;
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
            newPopulation = new ArrayList<>(newPopulation.subList(0, populationSize));

            population = newPopulation;

            // System.out.println(i);

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
            runningSum += solution.getFitness();
            if (runningSum > random) {
                return solution;
            }
        }

        System.out.println("Error selecting individual by roulette wheel");
        return population.get(population.size() - 1);
    }

    private ArrayList<SolutionRepresentation> crossover(SolutionRepresentation parent1, SolutionRepresentation parent2,
            int number) {
        if (parent1.getSolution().equals(parent2.getSolution())) {
            SolutionRepresentation newRepresentation = createSolutionRepresentationKMeans();
            parent2 = newRepresentation;
        }
        ArrayList<SolutionRepresentation> children = new ArrayList<SolutionRepresentation>();
        for (int i = 0; i < number; i++) {
            // select random route from both parents
            int randomRoute1 = (int) (Math.random() * parent1.getSolution().size());
            int randomRoute2 = (int) (Math.random() * parent2.getSolution().size());

            ArrayList<Patient> Route1 = parent1.getSolution().get(randomRoute1).getRoute().getPatients();
            ArrayList<Patient> Route2 = parent2.getSolution().get(randomRoute2).getRoute().getPatients();

            SolutionRepresentation child1 = parent1.copy();
            SolutionRepresentation child2 = parent2.copy();

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

            checkDistances(child1);
            checkDistances(child2);

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
                // check if nurse has capacity
                if ((nurse.getRoute().getTotalDemand() + patient.getDemand()) > nurse.getCapacity()) {
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

    private void insertPatientToRandomRoute(SolutionRepresentation solution, Patient patient) {
        int randomNurseIndex = (int) (Math.random() * solution.getSolution().size());
        insertPatientInRoute(solution.getSolution().get(randomNurseIndex).getRoute(), patient);
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

    private void insertPatientInRouteRandom(Route route, Patient patient) {
        int randomIndex = (int) (Math.random() * route.getPatients().size());
        route.getPatients().add(randomIndex, patient);
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
                    int mutation = (int) (Math.random() * 4);
                    switch (mutation) {
                        case 0:
                            mutateByReordering(nurse.getRoute(), i);
                            break;
                        case 1:
                            mutateBySwitchingNurse(solution, nurse.getRoute().getPatients().remove(i));
                            break;
                        case 2:
                            mutateBySwappingTwoPatients(nurse.getRoute(), i);
                            break;
                        case 3:
                            mutateBySwappingPatientsBetweenNurses(solution, nurse.getRoute(),
                                    nurse.getRoute().getPatients().remove(i));
                            break;
                        default:
                            break;
                    }
                }
            }
        }

    }

    private void mutateByReordering(Route route, int idx) {
        Patient patient = route.getPatients().remove(idx);
        insertPatientInRouteRandom(route, patient);
    }

    private void mutateBySwitchingNurse(SolutionRepresentation solution, Patient patient) {
        int newNurseIndex = (int) (Math.random() * solution.getSolution().size());
        insertPatientInRouteRandom(solution.getSolution().get(newNurseIndex).getRoute(), patient);
    }

    private void mutateBySwappingTwoPatients(Route route, int idx) {
        int randomIndex = (int) (Math.random() * route.getPatients().size());
        Collections.swap(route.getPatients(), idx, randomIndex);
    }

    private void mutateBySwappingPatientsBetweenNurses(SolutionRepresentation solution, Route route, Patient patient) {
        int randomNurseIndex = (int) (Math.random() * solution.getSolution().size());
        Nurse nurse = solution.getSolution().get(randomNurseIndex);
        if (nurse.getRoute().getPatients().size() == 0) {
            nurse.getRoute().getPatients().add(patient);
        } else {
            int randomPatientIndex = (int) (Math.random() * nurse.getRoute().getPatients().size());
            Patient randomPatient = nurse.getRoute().getPatients().remove(randomPatientIndex);
            insertPatientInRouteRandom(route, randomPatient);
            insertPatientInRouteRandom(nurse.getRoute(), patient);
        }
    }

    private void checkDistances(SolutionRepresentation solution) {
        // checks the distances in a solution.
        // loops over each route and over the patiens
        // if the distance between two patients is greater than 50, the route is split
        // lost patiens get reinserted
        for (Nurse nurse : solution.getSolution()) {
            int size = nurse.getRoute().getPatients().size();
            if (size == 1) {
                Patient patient = nurse.getRoute().getPatients().remove(0);
                insertPatientBasedOnNeighbourhood(solution, patient);
            } else if (size > 1) {
                ArrayList<Patient> removedPatients = new ArrayList<Patient>();
                ArrayList<Patient> patients = nurse.getRoute().getPatients();
                int breakPoint = 0;
                for (int i = 0; i < size - 1; i++) {
                    Patient patient = patients.get(i);
                    Patient nextPatient = patients.get(i + 1);
                    if (patient.distanceTo(nextPatient) > 50) {
                        breakPoint = i;
                        break;
                    }
                }
                if (breakPoint != 0) {
                    removedPatients.addAll(new ArrayList<Patient>(patients.subList(breakPoint, size)));
                    patients.removeAll(removedPatients);
                }
                for (Patient patient : removedPatients) {
                    Patient neighbor = patient.getNearestPatient().getPatient();
                    if (removedPatients.contains(neighbor)) {
                        insertPatientToRandomRoute(solution, patient);
                    } else {
                        insertPatientBasedOnNeighbourhood(solution, patient);
                    }
                }
            }
        }
    }

    private ArrayList<SolutionRepresentation> sortSolution(ArrayList<SolutionRepresentation> currentPopulation) {
        currentPopulation.sort((solution1, solution2) -> Double.compare(solution1.getFitness(),
                solution2.getFitness()));

        return currentPopulation;
    }

    public SolutionRepresentation getBestSolution() {
        SolutionRepresentation bestSolution = population.get(0);
        for (SolutionRepresentation solution : population) {
            if (solution.getFitness() < bestSolution.getFitness()) {
                bestSolution = solution;
            }
        }
        return bestSolution;
    }

    public double getBestFitness() {
        double bestFitness = Integer.MAX_VALUE;
        for (SolutionRepresentation solution : population) {
            double fitness = solution.getFitness();
            if (fitness < bestFitness) {
                bestFitness = fitness;
            }
        }
        return bestFitness;
    }

    public double getAverageFitness() {
        double totalFitness = 0;
        for (SolutionRepresentation solution : population) {
            totalFitness += solution.getFitness();
        }
        return totalFitness / populationSize;
    }

    public double getSumFitness() {
        double totalFitness = 0;
        for (SolutionRepresentation solution : population) {
            totalFitness += solution.getFitness();
        }
        return totalFitness;
    }
}
