import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

import utils.*;

public class NSGA2 {
    private final Image img;
    private final BufferedImage buffImg;
    private final int populationLength; // img_hight * img_width
    private int generations;
    private int amountOfSeconds;
    private boolean useTime;
    private double crossoverRate;
    private double individualMutationRate;
    private double probDistOfDifferentMutationTypes;
    private int populationSize;
    private int amountOfParents;
    private boolean useSmartPopGeneration;
    private ArrayList<SolutionRepresentation> population;
    private int amountOfCrossoverPoints;
    private double colorDiffCutOutForGeneration;

    private boolean useFrontier;
    private double edgeScoreMulti;
    private double connectivityScoreMulti;
    private double deviationScoreMulti;

    private int totalFrontierCount;
    private int sizeOfBestFrontier;

    // Define threadSafeRandom as a static field with initial value for each thread
    private static final ThreadLocal<Random> threadSafeRandom = ThreadLocal
            .withInitial(() -> new Random(ThreadLocalRandom.current().nextInt()));

    public NSGA2(Image img, BufferedImage bufferedImage, int generations, int populationSize, int amountOfSeconds,
            boolean useTime, double crossoverRate, double individualMutationRate,
            double probDistOfDifferentMutationTypes,
            int amountOfParents, boolean useSmartPopGeneration, boolean useFrontier, double edgeScoreMulti,
            double connectivityScoreMulti, double deviationScoreMulti, int amountOfCrossoverPoints, double colorDiffCutOutForGeneration) {
        this.img = img;
        this.populationLength = img.getHight() * img.getWidth();
        this.generations = generations;
        this.amountOfSeconds = amountOfSeconds;
        this.useTime = useTime;

        this.crossoverRate = crossoverRate;
        this.amountOfCrossoverPoints = amountOfCrossoverPoints;
        // prob of a individual getting a mutation
        this.individualMutationRate = individualMutationRate;
        // Prob of different mutation types; 0.4 40% small swap, 60% big swap
        this.probDistOfDifferentMutationTypes = probDistOfDifferentMutationTypes;

        this.populationSize = populationSize;
        this.amountOfParents = amountOfParents;
        this.useSmartPopGeneration = useSmartPopGeneration;
        this.colorDiffCutOutForGeneration = colorDiffCutOutForGeneration;
        this.population = null;

        this.useFrontier = useFrontier;
        this.edgeScoreMulti = edgeScoreMulti;
        this.connectivityScoreMulti = connectivityScoreMulti;
        this.deviationScoreMulti = deviationScoreMulti;
        this.buffImg = bufferedImage;

        this.totalFrontierCount = 0;
        this.sizeOfBestFrontier = 1;

        System.out.println("Creating initial pop");
        // init pop
        if (this.useSmartPopGeneration) {
            population = InitPop.generateSmartPopulation(populationSize, buffImg, colorDiffCutOutForGeneration);
        } else {
            population = InitPop.generatePopulation(populationSize, buffImg);
        }
        //to speed up the score calculating, calc them threaded at the start
        calcScoresThreaded();

        System.out.println("Initialized the genetic algortihm with:\n" + "Generations: " + generations
                + "\nPopulation size: " + populationSize + "\nInput image: " + img.getHight() + "x" + img.getWidth()
                + "\n");
    }

    public ArrayList<SolutionRepresentation> getPopulation() {
        return population;
    }

    public void run() {
        // run time based or amount of generations
        System.out.println("Running the genetic algorithm");
        if (useTime) {
            long startTime = System.currentTimeMillis();
            int i = 0;
            while ((System.currentTimeMillis() - startTime) < this.amountOfSeconds * 1000) {
                System.out.println("Gen: " + i);
                runGeneration();
//                runGenerationTimed();
                i++;
            }
        } else {
            for (int i = 0; i <= this.generations; i++) {
                System.out.println("Gen: " + i);
                runGeneration();
//                runGenerationTimed();
            }
        }
        Visualizer.visualizeFrontier(this.population.subList(0, sizeOfBestFrontier));
    }

    private void runGeneration() {
        // select parents
        ArrayList<SolutionRepresentation> parents;
        if (!useFrontier) {
            parents = this.selectTop(this.population, this.amountOfParents);
        } else {
            parents = this.selectBestFrontier(this.population, this.amountOfParents);
        }
        // preform crossover
        HashSet<SolutionRepresentation> children = this.crossOver(new HashSet<>(parents));
        // preform mutation
        HashSet<SolutionRepresentation> mutatedChildren = this.mutate(new HashSet<>(children));
        // add new children to pop
        this.population.addAll(mutatedChildren);
        // do this to speed up the process
        calcScoresThreaded();
        // select survivors
        if (!useFrontier) {
            this.population = this.selectTop(this.population, this.populationSize);
        } else {
            this.population = this.selectBestFrontier(this.population, this.populationSize);
        }

        //print best score
        double[] scores = this.population.get(0).getScore();
        double combinedScore = edgeScoreMulti * scores[0] + connectivityScoreMulti * scores[1]
                + deviationScoreMulti * scores[2];
        System.out.println("totalScore: " + combinedScore);
        System.out.println("edgeScore: " + edgeScoreMulti * scores[0]);
        System.out.println("connectivityScore: " + connectivityScoreMulti * scores[1]);
        System.out.println("deviationScore: " + deviationScoreMulti * scores[2]);
    }

    private void runGenerationTimed(){
        long startTime, endTime;

        // select parents
        startTime = System.nanoTime();
        ArrayList<SolutionRepresentation> parents;
        if (!useFrontier) {
            parents = this.selectTop(this.population, this.amountOfParents);
        } else {
            parents = this.selectBestFrontier(this.population, this.amountOfParents);
        }
        endTime = System.nanoTime();
        System.out.println("Select parents duration: " + (endTime - startTime) / 1.0e9 + " seconds");

        // perform crossover
        startTime = System.nanoTime();
        HashSet<SolutionRepresentation> children = this.crossOver(new HashSet<>(parents));
        endTime = System.nanoTime();
        System.out.println("Crossover duration: " + (endTime - startTime) / 1.0e9 + " seconds");

        // perform mutation
        startTime = System.nanoTime();
        HashSet<SolutionRepresentation> mutatedChildren = this.mutate(new HashSet<>(children));
        endTime = System.nanoTime();
        System.out.println("Mutation duration: " + (endTime - startTime) / 1.0e9 + " seconds");

        // add new children to pop
        startTime = System.nanoTime();
        this.population.addAll(mutatedChildren);
        endTime = System.nanoTime();
        System.out.println("Adding new children duration: " + (endTime - startTime) / 1.0e9 + " seconds");

        // select survivors
        // do this to speed up the process
        //calcScoresThreaded();
        startTime = System.nanoTime();
        if (!useFrontier) {
            this.population = this.selectTop(this.population, this.populationSize);
        } else {
            this.population = this.selectBestFrontier(this.population, this.populationSize);
        }
        endTime = System.nanoTime();
        System.out.println("Select survivors duration: " + (endTime - startTime) / 1.0e9 + " seconds");

        //print best score
        double[] scores = this.population.get(0).getScore();
        double combinedScore = edgeScoreMulti * scores[0] + connectivityScoreMulti * scores[1]
                + deviationScoreMulti * scores[2];
        System.out.println("totalScore: " + combinedScore);
        System.out.println("edgeScore: " + edgeScoreMulti * scores[0]);
        System.out.println("connectivityScore: " + connectivityScoreMulti * scores[1]);
        System.out.println("deviationScore: " + deviationScoreMulti * scores[2]);
    }

    private ArrayList<SolutionRepresentation> selectTop(ArrayList<SolutionRepresentation> pop, int amount) {
        pop.sort(new Comparator<SolutionRepresentation>() {
            @Override
            public int compare(SolutionRepresentation o1, SolutionRepresentation o2) {
                double[] scores1 = o1.getScore();
                double combinedScore1 = edgeScoreMulti * scores1[0] + connectivityScoreMulti * scores1[1]
                        + deviationScoreMulti * scores1[2];

                double[] scores2 = o2.getScore();
                double combinedScore2 = edgeScoreMulti * scores2[0] + connectivityScoreMulti * scores2[1]
                        + deviationScoreMulti * scores2[2];

                return Double.compare(combinedScore2, combinedScore1); // Descending order -> highest score on top
            }
        });
        return new ArrayList<>(pop.subList(0, amount));
    }

    public ArrayList<SolutionRepresentation> selectBestFrontier(ArrayList<SolutionRepresentation> pop, int amount) {
        // slide 49-61 (50 for overview) of BioAI_2024_Week_12_v2.pdf
        // first, get a list of the frontiers
        ArrayList<ArrayList<SolutionRepresentation>> frontiers = new ArrayList<>();
        // keep track of the amount
        this.totalFrontierCount = 0;
        // copy this to keep track of the removed individuals
        ArrayList<SolutionRepresentation> alteredPop = new ArrayList<>(pop);
        //get the new frontiers recursively
        while(totalFrontierCount < amount){
            ArrayList<SolutionRepresentation> newFrontier = this.getFrontier(alteredPop);
            frontiers.add(newFrontier);
            alteredPop.removeAll(newFrontier);
            totalFrontierCount += newFrontier.size();
        }
        //get results
        ArrayList<SolutionRepresentation> result = new ArrayList<>();
        if (totalFrontierCount == amount){
            //lucky, no crowding required
            for (ArrayList<SolutionRepresentation> frontier: frontiers) {
                result.addAll(frontier);
            }
        } else {
            for (int i = 0; i < frontiers.size(); i++) {
                //only do crowding for the last element
                if (i == frontiers.size() - 1){
                    int amountToAdd = frontiers.get(i).size() - (totalFrontierCount - amount);
                    result.addAll(frontierCrowding(frontiers.get(i), amountToAdd));
                } else {
                    result.addAll(frontiers.get(i));
                }
            }
        }
        //save Best frontier size
        if (frontiers.size() == 1) {
            this.sizeOfBestFrontier = amount;
        } else {
            this.sizeOfBestFrontier = frontiers.get(0).size();
        }
        return result;
    }

    private ArrayList<SolutionRepresentation> getFrontier(ArrayList<SolutionRepresentation> allIndividuals){
        //keep track of the frontier
        ArrayList<SolutionRepresentation> frontier = new ArrayList<>();
        //loop through the entire list
        for (SolutionRepresentation individual: allIndividuals) {
            //get the score to compare later
            double[] newScore = individual.getScore();
            if(frontier.size() != 0) {
                //keep track of individuals to remove from the frontier if they are dominated
                ArrayList<SolutionRepresentation> toRemove = new ArrayList<>();
                boolean addNewIndividual = false;

                for (SolutionRepresentation frontierIndividual: frontier) {
                    //get the frontier score to compare
                    double[] frontierScore = frontierIndividual.getScore();

                    if (newScore[0] > frontierScore[0] && newScore[1] < frontierScore[1] && newScore[2] < frontierScore[2]){
                        //fully dominated -> remove this frontierIndividual
                        toRemove.add(frontierIndividual);
                        //allow new individual to be added
                        addNewIndividual = true;
                    } else if(newScore[0] > frontierScore[0] || newScore[1] < frontierScore[1] || newScore[2] < frontierScore[2]){
                        //partly dominated -> don't remove this frontierIndividual, but allow new individual to be added
                        addNewIndividual = true;
                    }
                }
                // execute results
                frontier.removeAll(toRemove);
                if (addNewIndividual){
                    frontier.add(individual);
                }
            }else {
                frontier.add(individual);
            }
        }
        return frontier;
    }

    private ArrayList<SolutionRepresentation> frontierCrowding(ArrayList<SolutionRepresentation> frontier, int amount){
        //initialize crowding distance for each individual
        frontier.forEach(individual -> individual.setCrowdingDistance(0.0));
        //keep track of boundary individuals
        ArrayList<SolutionRepresentation> frontierBoundaries = new ArrayList<>();
        //for each score
        //do this to go one side of the cuboid at a time, per score
        for (int objIndex = 0; objIndex < 3; objIndex++) {
            final int index = objIndex;

            //sort individuals based on the score
            frontier.sort(Comparator.comparingDouble(o -> o.getScore()[index]));

            //set the boundary individuals' crowding distance to infinity
            frontier.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
            frontier.get(frontier.size() - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);
            //keep track of boundary individuals
            frontierBoundaries.add(frontier.get(0));
            frontierBoundaries.add(frontier.get(frontier.size() - 1));

            //calculate crowding distance for intermediate individuals
            double minObjectiveValue = frontier.get(0).getScore()[index];
            double maxObjectiveValue = frontier.get(frontier.size() - 1).getScore()[index];
            //get range
            double range = maxObjectiveValue - minObjectiveValue;

            for (int i = 1; i < frontier.size() - 1; i++) {
                double prevDist = frontier.get(i).getCrowdingDistance();
                //only update if the current distance is not infinity (preserve the boundaries of the front)
                if (!Double.isInfinite(prevDist)) {
                    double distance = frontier.get(i + 1).getScore()[index] - frontier.get(i - 1).getScore()[index];
                    if (range > 0) {
                        double normalizedDistance = distance / range;
                        frontier.get(i).setCrowdingDistance(frontier.get(i).getCrowdingDistance() + normalizedDistance);
                    }
                }
            }
        }
        //sort individuals by decreasing crowding distance
        frontier.sort((o1, o2) -> Double.compare(o2.getCrowdingDistance(), o1.getCrowdingDistance()));
        //return the crowded result
        if (amount < frontierBoundaries.size()){
            //this happens when the amount of things to add is smaller than the amount of boundaries from the frontier
            //in this case, we select random ones from the boundaries
            Collections.shuffle(frontierBoundaries);
            return new ArrayList<>(frontierBoundaries.subList(0, amount));
        } else {
            //return best X amount
            return new ArrayList<>(frontier.subList(0, amount));
        }
    }

    private HashSet<SolutionRepresentation> crossOver(HashSet<SolutionRepresentation> parents) {
        // Concurrent collection to store individuals
        ConcurrentLinkedQueue<SolutionRepresentation> children = new ConcurrentLinkedQueue<>();
        // Custom ForkJoinPool to control the parallelism level
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        // Copy a list of parents for access
        ArrayList<SolutionRepresentation> parentList = new ArrayList<>(parents);
        //todo try changing to for and selecting 2 parents at a time
        try {
            customThreadPool.submit(() -> parents.parallelStream().forEach(parent -> {
                // select other parent
                Random rnd = threadSafeRandom.get();
                SolutionRepresentation parent2;
                do {
                    parent2 = parentList.get(rnd.nextInt(parentList.size()));
                } while (parent == parent2);// ensure the parents are different
                // preform crossover with these parents
                SolutionRepresentation[] newChildren = individualCrossover(parent, parent2);
                children.add(newChildren[0]);
                children.add(newChildren[1]);
            })).get(); // Wait for all tasks to complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }
        return new HashSet<>(children);
    }

    public SolutionRepresentation[] individualCrossover(SolutionRepresentation parent1,
            SolutionRepresentation parent2) {
        // result
        SolutionRepresentation[] children = new SolutionRepresentation[2];
        Random rnd = threadSafeRandom.get();
        // check if we do crossover
        if (rnd.nextDouble() < this.crossoverRate) {
            int amountOfCrossoverPoints = rnd.nextInt(this.amountOfCrossoverPoints) + 1;
            // select the points to do crossover at random
            ArrayList<Integer> crossoverPoints = new ArrayList<>();
            for (int i = 0; i < amountOfCrossoverPoints; i++) {
                int newPoint;
                do {
                    newPoint = rnd.nextInt(parent1.getSolution().size());
                } while (crossoverPoints.contains(newPoint));// dont select the same point twice
                crossoverPoints.add(newPoint);
            }
            //prevent children and parents from using the same pixels
            ArrayList<Pixel> newPixelsChild1 = new ArrayList<Pixel>();
            ArrayList<Pixel> newPixelsChild2 = new ArrayList<Pixel>();
            for (Pixel pixel: parent1.getSolution()) {
                newPixelsChild1.add(new Pixel(pixel));
            }
            for (Pixel pixel: parent2.getSolution()) {
                newPixelsChild2.add(new Pixel(pixel));
            }
            SolutionRepresentation child1 = new SolutionRepresentation(newPixelsChild1, parent1.getImageWidth());
            SolutionRepresentation child2 = new SolutionRepresentation(newPixelsChild2, parent2.getImageWidth());
            // start cutting
            Collections.sort(crossoverPoints);
            int prevPoint = -1;
            for (Integer point : crossoverPoints) {
                if (prevPoint == -1) {
                    prevPoint = point;
                } else {
                    List<Pixel> partP1 = child1.getSolution().subList(prevPoint, point);
                    List<Pixel> partP2 = child2.getSolution().subList(prevPoint, point);

                    //TODO change the connection instead of changin the pixels
                    for (int i = 0; i < partP1.size(); i++) {
                        PossibleConnections connectionP1 = partP1.get(i).connection;
                        PossibleConnections connectionP2 = partP2.get(i).connection;
                        partP1.get(i).setConnection(connectionP2);
                        partP2.get(i).setConnection(connectionP1);
                    }
                    // child1.getSolution().removeAll(partP1);
                    // child1.getSolution().addAll(prevPoint, partP2);

                    // child2.getSolution().removeAll(partP2);
                    // child2.getSolution().addAll(prevPoint, partP1);

                    prevPoint = point;
                }
            }
            children[0] = child1;
            children[1] = child2;

        } else {
            //these have to be new elements, not copies
            children[0] = new SolutionRepresentation(parent1);
            children[1] = new SolutionRepresentation(parent2);
        }
        return children;
    }

    private HashSet<SolutionRepresentation> mutate(HashSet<SolutionRepresentation> children) {
        // Concurrent collection to store individuals
        ConcurrentLinkedQueue<SolutionRepresentation> mutatedChildren = new ConcurrentLinkedQueue<>();
        // Custom ForkJoinPool to control the parallelism level
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            customThreadPool.submit(() -> children.parallelStream().forEach(child -> {
                mutatedChildren.add(individualMutate(child));
            })).get(); // Wait for all tasks to complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }
        return new HashSet<>(mutatedChildren);
    }

    private SolutionRepresentation individualMutate(SolutionRepresentation child) {
        Random rnd = threadSafeRandom.get();
        if (rnd.nextDouble() < this.probDistOfDifferentMutationTypes) {
            return mutationType1(child);
        } else {
            return mutationType2(child);
        }
    }

    public SolutionRepresentation mutationType1(SolutionRepresentation child) {
        Random rnd = threadSafeRandom.get();
        for (Pixel pixel : child.getSolution()) {
            if (rnd.nextDouble() < this.individualMutationRate) {
                //this is not selecting OOB connections
                ArrayList<Integer> dirOptions = new ArrayList<Integer>();
                for (int i = 0; i < pixel.neighbors.size(); i++) {
                    if (pixel.neighbors.get(i) != null) {
                        dirOptions.add(i);
                    }
                }
                int newDir = dirOptions.get(rnd.nextInt(dirOptions.size()));
                pixel.setConnection(PossibleConnections.values()[newDir]);
            }
        }
        return child;
    }

    private SolutionRepresentation mutationType2(SolutionRepresentation child) {
        // todo maybe implement other mutation
        // other mutation ideas??
        return child;
    }

    private void calcScoresThreaded(){
        // Custom ForkJoinPool to control the parallelism level
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            customThreadPool.submit(() -> population.parallelStream().forEach(SolutionRepresentation::getScore)).get(); // Wait for all tasks to complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }
    }
}
