import utils.Image;
import utils.InitPop;

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
        int populationSize = 1;
        int amountOfSeconds = 360;
        boolean useTime = false;
        double crossoverRate = 0.8;
        double individualMutationRate = 0.2;
        double probDistOfDifferentMutationTypes = 0.5;
        int amountOfParents = 120;
        boolean useSmartPopGeneration = true;

        NSGA2 GA = new NSGA2(img, imgPath, generations, populationSize, amountOfSeconds, useTime, crossoverRate, individualMutationRate, probDistOfDifferentMutationTypes, amountOfParents, useSmartPopGeneration);
        //System.out.println("test");

        System.out.println(GA.getPopulation().get(0));
        GA.getPopulation().get(0).generateSegments();
        System.out.println(GA.getPopulation().get(0).getSegments().size());

        System.out.println(GA.getPopulation().get(0).getSolution().get(0).getDistance());

        GA.run();
    }
}
