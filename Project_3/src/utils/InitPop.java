package utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.PossibleConnections.*;

public abstract class InitPop {

    // Define threadSafeRandom as a static field with initial value for each thread
    private static final ThreadLocal<Random> threadSafeRandom = ThreadLocal.withInitial(() -> new Random(ThreadLocalRandom.current().nextInt()));


    //Create the individuals in parallel for speed
    public static ArrayList<SolutionRepresentation> generateSmartPopulation(int populationSize, Image img, int populationLength) {

        // Concurrent collection to store individuals
        ConcurrentLinkedQueue<SolutionRepresentation> pop = new ConcurrentLinkedQueue<>();

        // Custom ForkJoinPool to control the parallelism level, you can also use the common pool
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            customThreadPool.submit(() -> {
               IntStream.range(0, populationSize).parallel().forEach(i -> {
                   //dont pass the image directly but create a new list of pixels each time instead, this prevents threading issues
                    Pixel[][] pixels = img.getPixels();
                    //Add the individual to the concurrent collection
                    pop.add(generateSmartIndividual(pixels, populationLength));
                    System.out.println("created individual");
                });
            }).get(); // Waiting for all tasks to complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
        }

        return new ArrayList<>(pop);
    }

    private static SolutionRepresentation generateSmartIndividual(Pixel[][] pixels, int populationLength){
        int width = pixels[0].length;
        int height = pixels.length;
        //track a list of options for pixels
        HashSet<Pixel> pixelOptions = new HashSet<Pixel>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p = pixels[i][j];
                pixelOptions.add(p);
            }
        }
        ArrayList<Pixel> solution = new ArrayList<Pixel>();
        Set<Integer> pixelKeysVisited = new HashSet<Integer>();

        while (!pixelOptions.isEmpty()){
            //thread safe random
            Random rnd = threadSafeRandom.get();
            //select a random pixel to start a region
            List<Pixel> pixelOptionsTemp = new ArrayList<Pixel>(pixelOptions);
            Pixel currentPixel = pixelOptionsTemp.get(rnd.nextInt(pixelOptions.size()));
            pixelKeysVisited.add(currentPixel.getKey());
            pixelOptions.remove(currentPixel);
            //set it to connect to itsself (working backwards)
            currentPixel.setConnection(PossibleConnections.NONE);
            //search for a neighbour
            boolean search = true;
            //Maximum length of a "snake" of connected pixels
            int max = 10000;
            max = Integer.MAX_VALUE;
            int j = 0;
            //keep track of neighbours
            ArrayList<Pixel> possibleNeighbours = new ArrayList<>();
            //keep track of the visited each loop/segment/snake
            Set<Integer> visitedThisLoop = new HashSet<>();
            visitedThisLoop.add(currentPixel.getKey());
            while (search){
                //filter new neighbours
                ArrayList<Pixel> newNeighbours = new ArrayList<>(currentPixel.neighbors);
                List<Pixel> newNeighbourOptions = currentPixel.neighbors.stream()
                        .filter(Objects::nonNull)
                        .filter(neighbour -> !pixelKeysVisited.contains(neighbour.getKey()))
                        .collect(Collectors.toList());
                //add the new neighbours
                possibleNeighbours.addAll((newNeighbourOptions));
                //if no possibilities or "snake" length too long, stop the search and go to the next random pixel to start a new "snake"
                if(possibleNeighbours.size() == 0 || j > max){
                    search = false;
                } else {
                    //select the neighbour by random number
                    Pixel neighbour = possibleNeighbours.get(rnd.nextInt(possibleNeighbours.size()));
                    //get the location of an element in the new segment/snake that borders the selected neighbour
                    int nr = -1;
                    for(int z = 0; z < neighbour.neighbors.size(); z++){
                        if(neighbour.neighbors.get(z) != null){
                            if(visitedThisLoop.contains(neighbour.neighbors.get(z).getKey())){
                                nr = z;
                                break;
                            }
                        }
                    }
                    //connect the neighbour to the current segment
                    neighbour.setConnection(PossibleConnections.values()[nr]);
                    //add neighbour to visited
                    pixelKeysVisited.add(neighbour.getKey());
                    visitedThisLoop.add(neighbour.getKey());
                    //remove as an option
                    possibleNeighbours.remove(neighbour);
                    pixelOptions.remove(neighbour);
                    //set current to neighbour
                    currentPixel = neighbour;
                    //increase the size of the "snake/segment"
                    j++;
                }
            }
        }
        //actually add the new pixels to the solution
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p = pixels[i][j];
                solution.add(p);
            }
        }

        return new SolutionRepresentation(solution, pixels[0].length);

    }

    public static ArrayList<SolutionRepresentation> generatePopulation(int populationSize, Image img) {
        //could change to threading aswell
        ArrayList<SolutionRepresentation> population = new ArrayList<SolutionRepresentation>();

        for (int i = 0; i < populationSize; i++) {
            population.add(generateSolutionRand(img));
        }

        return population;
    }

    private static SolutionRepresentation generateSolutionRand(Image img) {

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

    public static Image loadImage(String path) {

        try {
            BufferedImage imgBuf = ImageIO.read(new File(path));
            // imgBuf = ImageIO.read(new File("test.png"));
            //System.out.println("Found image with width: " + imgBuf.getWidth() + " and height: " + imgBuf.getHeight());
            Image img = new Image(imgBuf.getHeight(), imgBuf.getWidth());

            int pixelID = 0;
            int hight = imgBuf.getHeight();
            int width = imgBuf.getWidth();

            for (int y = 0; y < hight; y++) {
                for (int x = 0; x < width; x++) {
                    // Get the RGB value of the pixel
                    int rgbValue = imgBuf.getRGB(x, y);
                    int red = (rgbValue >> 16) & 0xff;
                    int green = (rgbValue >> 8) & 0xff;
                    int blue = (rgbValue) & 0xff;
                    RGBRepresentation color = new RGBRepresentation(red, green, blue);

                    Pixel p = new Pixel(pixelID, color);
                    img.setPixel(y, x, p);
                    pixelID++;
                }
            }

            img.generateNeighbors();

            //System.out.println("Image loaded successfully");
            return img;
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        System.out.println("Error: Could not load image");
        return null;
    }

}
