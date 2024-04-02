import utils.Image;
import utils.InitPop;
import utils.SolutionRepresentation;
import utils.Visualizer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class MOOP {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();
        frame.setTitle("Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String imgPath = "Project_3/training_imgs/86016/Test image.jpg";
        // imgPath = "Project_3/training_imgs/ForTesting3x3White.jpg";
        // imgPath = "Project_3/training_imgs/ForTesting10x10White.jpg";
        BufferedImage imgBuff = ImageIO.read(new File(imgPath));
        Image img = InitPop.loadImage(imgBuff);
        img.printImage();
        System.out.println("\n");
        // img.show();

        // Testing the neighbors
        // System.out.println(img.getPixels()[0][0].getKey() + " " +
        // img.getPixels()[0][0].getNeighbors());

        int generations = 5;
        int populationSize = 1;
        int amountOfSeconds = 360;
        boolean useTime = false;
        double crossoverRate = 1;
        double individualMutationRate = 0.5;
        double probDistOfDifferentMutationTypes = 0.5;
        int amountOfParents = 4;
        boolean useSmartPopGeneration = true;

        boolean useFrontier = false;
        double edgeScoreMulti = 0.3;
        double connectivityScoreMulti = 0.4;
        double deviationScoreMulti = 0.4;

        NSGA2 GA = new NSGA2(img, imgBuff, generations, populationSize, amountOfSeconds, useTime, crossoverRate,
                individualMutationRate, probDistOfDifferentMutationTypes, amountOfParents, useSmartPopGeneration,
                useFrontier, edgeScoreMulti, connectivityScoreMulti, deviationScoreMulti);
        // System.out.println("test");

        // GA.run();
        // System.out.println(GA.getPopulation().get(0));
        // SolutionRepresentation test = GA.getPopulation().get(0);
        // System.out.println(test.getScore()[0]);
        // System.out.println(test.getScore()[1]);
        // System.out.println(test.getScore()[2]);
        // for (SolutionRepresentation s : GA.getPopulation()) {
        // System.out.println(s);
        // }

        // Testing the crossover and mutation
        // System.out.println("parents");
        // System.out.println(GA.getPopulation().get(0));
        // System.out.println(GA.getPopulation().get(1));
        // SolutionRepresentation[] children =
        // GA.individualCrossover(GA.getPopulation().get(1),
        // GA.getPopulation().get(0));
        // System.out.println("children");
        // System.out.println(children[0]);
        // System.out.println("Mutated Child 0");
        // System.out.println(GA.mutationType1(children[0]));

        SolutionRepresentation individual = GA.getPopulation().get(0);

        Visualizer.visualizeSolution(individual);

        // System.out.println(GA.getPopulation().get(0).getSolution().get(0).getDistance());

    }
}
