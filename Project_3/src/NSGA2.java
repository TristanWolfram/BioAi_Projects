import java.util.ArrayList;

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

    public NSGA2(Image img, String imgPath, int generations, int populationSize, int amountOfSeconds, boolean useTime, double crossoverRate, double individualMutationRate, double probDistOfDifferentMutationTypes, int amountOfParents, boolean useSmartPopGeneration) {
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


    public void run() {
        System.out.println("Running the genetic algorithm");
    }

    public ArrayList<SolutionRepresentation> getPopulation() {
        return population;
    }
}
