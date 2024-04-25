import utils.Image;
import utils.InitPop;
import utils.SolutionRepresentation;
import utils.Visualizer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class MOOP {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();
        frame.setTitle("Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String imgPath = "Project_3/training_imgs/147091/Test image.jpg";
        // imgPath = "Project_3/training_imgs/ForTesting3x3White.jpg";
        // imgPath = "Project_3/training_imgs/ForTesting10x10White.jpg";
        BufferedImage imgBuff = ImageIO.read(new File(imgPath));
        Image img = InitPop.loadImage(imgBuff);
        img.printImage();
        System.out.println("\n");
        // img.show();

        int generations = 5;
        int populationSize = 12;
        int amountOfSeconds = 360;
        boolean useTime = false;
        double crossoverRate = 1;
        int amountOfCrossoverPoints = 20;
        // might have to be very high in order to actually cause a change to the
        // segments
        double individualMutationRate = 0.001;
        double probDistOfDifferentMutationTypes = 1;
        int amountOfParents = 4;
        boolean useSmartPopGeneration = true;
        double colorDiffCutOutForGeneration = 100;

        boolean useFrontier = true;
        //
        double edgeScoreMulti = 0.0001; // maximize
        double connectivityScoreMulti = -0.05; // minimize
        double deviationScoreMulti = -0.00002; // minimize

        NSGA2 GA = new NSGA2(img, imgBuff, generations, populationSize, amountOfSeconds, useTime, crossoverRate,
                individualMutationRate, probDistOfDifferentMutationTypes, amountOfParents, useSmartPopGeneration,
                useFrontier, edgeScoreMulti, connectivityScoreMulti, deviationScoreMulti, amountOfCrossoverPoints,
                colorDiffCutOutForGeneration);

        // SolutionRepresentation individual = GA.getPopulation().get(0);

        // Visualizer.visualizeSolution(individual);

        GA.run();

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

        // System.out.println(GA.getPopulation().get(0).getSolution().get(0).getDistance());

    }
}
