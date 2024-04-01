import utils.Image;
import utils.InitPop;
import utils.SolutionRepresentation;

import javax.swing.JFrame;

public class MOOP {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String imgPath = "Project_3/training_imgs/86016/Test image.jpg";
        //imgPath = "Project_3/training_imgs/ForTesting3x3White.jpg";
        //imgPath = "Project_3/training_imgs/ForTesting10x10White.jpg";

        Image img = InitPop.loadImage(imgPath);
        img.printImage();
        System.out.println("\n");
        // img.show();

        // Testing the neighbors
        // System.out.println(img.getPixels()[0][0].getKey() + " " +
        // img.getPixels()[0][0].getNeighbors());

        int generations = 500;
        int populationSize = 6;
        int amountOfSeconds = 360;
        boolean useTime = false;
        double crossoverRate = 0.8;
        double individualMutationRate = 0.2;
        double probDistOfDifferentMutationTypes = 0.5;
        int amountOfParents = 2;
        boolean useSmartPopGeneration = true;

        boolean useFrontier = false;
        double edgeScoreMulti = 0.3;
        double connectivityScoreMulti = 0.4;
        double deviationScoreMulti = 0.4;

        NSGA2 GA = new NSGA2(img, imgPath, generations, populationSize, amountOfSeconds, useTime, crossoverRate, individualMutationRate, probDistOfDifferentMutationTypes, amountOfParents, useSmartPopGeneration, useFrontier, edgeScoreMulti, connectivityScoreMulti, deviationScoreMulti);
        //System.out.println("test");

        // GA.run();
       // System.out.println(GA.getPopulation().get(0));
        SolutionRepresentation test = GA.getPopulation().get(0);
        System.out.println(test.getScore()[0]);
        System.out.println(test.getScore()[1]);
        System.out.println(test.getScore()[2]);
        // System.out.println(GA.getPopulation().get(0).getSegments().size());

        // System.out.println(GA.getPopulation().get(0).getSolution().get(0).getDistance());

        
    }
}
