import java.util.ArrayList;

import utils.*;

public class NSGA2 {
    private final Image img;
    final int populationLength; // img_hight * img_width
    int populationSize;
    int generations;

    ArrayList<SolutionRepresentation> population;

    public NSGA2(Image img, int generations, int populationSize) {
        this.img = img;
        this.populationLength = img.getHight() * img.getWidth();
        this.generations = generations;
        this.populationSize = populationSize;

        this.population = generatePopulation();

        System.out.println("Initialized the genetic algortihm with:\n" + "Generations: " + generations
                + "\nPopulation size: " + populationSize + "\nInput image: " + img.getHight() + "x" + img.getWidth()
                + "\n");
    }

    private ArrayList<SolutionRepresentation> generatePopulation() {
        ArrayList<SolutionRepresentation> population = new ArrayList<SolutionRepresentation>();

        for (int i = 0; i < populationSize; i++) {
            population.add(generateSolutionRand());
        }

        return population;
    }

    private SolutionRepresentation generateSolutionRand() {

        ArrayList<Pixel> solution = new ArrayList<Pixel>();

        // flatten img
        Pixel[][] pixels = img.getPixels();
        for (int i = 0; i < img.getHight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                Pixel p = pixels[i][j];
                // set connection of the pixel to a random one
                p.setConnection(
                        PossibleConnections.values()[(int) (Math.random() *
                                4)]);
                // p.setConnection(PossibleConnections.RIGHT);
                solution.add(p);
            }
        }

        return new SolutionRepresentation(solution, img.getWidth());
    }

    public void run() {
        System.out.println("Running the genetic algorithm");
    }

    public ArrayList<SolutionRepresentation> getPopulation() {
        return population;
    }
}
