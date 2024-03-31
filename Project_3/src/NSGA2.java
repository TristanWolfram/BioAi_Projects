import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import utils.*;

public class NSGA2 {
    private final Image img;
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

    private boolean useFrontier;
    private double edgeScoreMulti;
    private double connectivityScoreMulti;
    private double deviationScoreMulti;

    // Define threadSafeRandom as a static field with initial value for each thread
    private static final ThreadLocal<Random> threadSafeRandom = ThreadLocal.withInitial(() -> new Random(ThreadLocalRandom.current().nextInt()));

    public NSGA2(Image img, String imgPath, int generations, int populationSize, int amountOfSeconds, boolean useTime, double crossoverRate, double individualMutationRate, double probDistOfDifferentMutationTypes,
                 int amountOfParents, boolean useSmartPopGeneration, boolean useFrontier, double edgeScoreMulti, double connectivityScoreMulti, double deviationScoreMulti) {
        this.img = img;
        this.populationLength = img.getHight() * img.getWidth();
        this.generations = generations;
        this.amountOfSeconds = amountOfSeconds;
        this.useTime = useTime;

        this.crossoverRate = crossoverRate;
        // prob of a individual getting a mutation
        this.individualMutationRate = individualMutationRate;
        // Prob of different mutation types; 0.4 40% small swap, 60% big swap
        this.probDistOfDifferentMutationTypes = probDistOfDifferentMutationTypes;

        this.populationSize = populationSize;
        this.amountOfParents = amountOfParents;
        this.useSmartPopGeneration = useSmartPopGeneration;
        this.population = null;

        this.useFrontier = useFrontier;
        this.edgeScoreMulti = edgeScoreMulti;
        this.connectivityScoreMulti = connectivityScoreMulti;
        this.deviationScoreMulti = deviationScoreMulti;


        //init pop
        if (this.useSmartPopGeneration){
            population = InitPop.generateSmartPopulation(populationSize, img, populationLength);
        } else {
            population = InitPop.generatePopulation(populationSize, img);
        }

        System.out.println("Initialized the genetic algortihm with:\n" + "Generations: " + generations
                + "\nPopulation size: " + populationSize + "\nInput image: " + img.getHight() + "x" + img.getWidth()
                + "\n");
    }

    public ArrayList<SolutionRepresentation> getPopulation() {
        return population;
    }

    public void run() {
        //run time based or amount of generations
        if(useTime){
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < this.amountOfSeconds * 1000){
                runGeneration();
            }
        } else {
            for (int i = 0; i <= this.generations; i++){
                runGeneration();
            }
        }
        System.out.println("Running the genetic algorithm");
    }

    private void runGeneration(){
        //select parents
        ArrayList<SolutionRepresentation> parents;
        if (useFrontier){
            parents = this.selectTop(this.population, this.amountOfParents);
        } else {
            parents = this.selectBestFrontier(this.population, this.amountOfParents);
        }
        //preform crossover
        HashSet<SolutionRepresentation> children = this.crossOver(new HashSet<>(parents));
        //preform mutation
        HashSet<SolutionRepresentation> mutatedChildren = this.mutate(new HashSet<>(children));
        //add new children to pop
        this.population.addAll(mutatedChildren);
        //select survivors
        if (useFrontier){
            this.population = this.selectTop(this.population, this.populationSize);
        } else {
            this.population = this.selectBestFrontier(this.population, this.populationSize);
        }
    }

    private ArrayList<SolutionRepresentation> selectTop(ArrayList<SolutionRepresentation> pop, int amount){
        Collections.sort(pop, new Comparator<SolutionRepresentation>() {
            @Override
            public int compare(SolutionRepresentation o1, SolutionRepresentation o2) {
                double[] scores1 = o1.getScore();
                double combinedScore1 = edgeScoreMulti * scores1[0] + connectivityScoreMulti * scores1[1] + deviationScoreMulti * scores1[2];

                double[] scores2 = o2.getScore();
                double combinedScore2 = edgeScoreMulti * scores2[0] + connectivityScoreMulti * scores2[1] + deviationScoreMulti * scores2[2];

                return Double.compare(combinedScore2, combinedScore1); // Descending order -> highest score on top
            }
        });
        return new ArrayList<>(pop.subList(0, amount));
    }

    private ArrayList<SolutionRepresentation> selectBestFrontier(ArrayList<SolutionRepresentation> pop, int amount){
        //todo fancy stuff with pareto optimal frontier etc.
        //slide 49-61 (50 for overview) of BioAI_2024_Week_12_v2.pdf
        return new ArrayList<>(pop);
    }

    private HashSet<SolutionRepresentation> crossOver(HashSet<SolutionRepresentation> parents){
        // Concurrent collection to store individuals
        ConcurrentLinkedQueue<SolutionRepresentation> children = new ConcurrentLinkedQueue<>();
        // Custom ForkJoinPool to control the parallelism level
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        // Copy a list of parents for access
        ArrayList<SolutionRepresentation> parentList = new ArrayList<>(parents);
        try {
            customThreadPool.submit(() ->
                    parents.parallelStream().forEach(parent -> {
                        // select other parent
                        Random rnd = threadSafeRandom.get();
                        SolutionRepresentation parent2;
                        do {
                            parent2 = parentList.get(rnd.nextInt(parentList.size()));
                        } while (parent == parent2);//ensure the parents are different
                        //preform crossover with these parents
                        SolutionRepresentation[] newChildren = individualCrossover(parent, parent2);
                        children.add(newChildren[0]);
                        children.add(newChildren[1]);
                    })
            ).get(); // Wait for all tasks to complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }
        return new HashSet<>(children);
    }

    private SolutionRepresentation[] individualCrossover(SolutionRepresentation parent1, SolutionRepresentation parent2){
        //result
        SolutionRepresentation[] children = new SolutionRepresentation[2];
        Random rnd = threadSafeRandom.get();
        //check if we do crossover
        if (rnd.nextDouble() < this.crossoverRate){
            //todo preform crossover logic with the 2 parents
        } else {
            children[0] = parent1;
            children[1] = parent2;
        }
        return children;
    }


    private HashSet<SolutionRepresentation> mutate(HashSet<SolutionRepresentation> children) {
        // Concurrent collection to store individuals
        ConcurrentLinkedQueue<SolutionRepresentation> mutatedChildren = new ConcurrentLinkedQueue<>();
        // Custom ForkJoinPool to control the parallelism level
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            customThreadPool.submit(() ->
                    children.parallelStream().forEach(child -> {
                        mutatedChildren.add(individualMutate(child));
                    })
            ).get(); // Wait for all tasks to complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }
        return new HashSet<>(mutatedChildren);
    }

    private SolutionRepresentation individualMutate(SolutionRepresentation child) {
        Random rnd = threadSafeRandom.get();
        if (rnd.nextDouble() < this.individualMutationRate){
            //could do just one type of mutation
            if (rnd.nextDouble() < this.probDistOfDifferentMutationTypes){
                return mutationType1(child);
            } else {
                return mutationType2(child);
            }
        } else {
            return child;
        }
    }

    private SolutionRepresentation mutationType1(SolutionRepresentation child){
        //todo implement mutation
        //simple flipping arrows?
        return child;
    }

    private SolutionRepresentation mutationType2(SolutionRepresentation child){
        //todo implement other mutation
        //other mutation ideas??
        return child;
    }

}
