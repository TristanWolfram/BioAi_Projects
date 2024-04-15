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
    private static final ThreadLocal<Random> threadSafeRandom = ThreadLocal
            .withInitial(() -> new Random(ThreadLocalRandom.current().nextInt()));

    // Create the individuals in parallel for speed
    public static ArrayList<SolutionRepresentation> generateSmartPopulation(int populationSize, BufferedImage buffImg,
            double colorDiffCutOutForGeneration) {

        // Concurrent collection to store individuals
        ConcurrentLinkedQueue<SolutionRepresentation> pop = new ConcurrentLinkedQueue<>();

        // Custom ForkJoinPool to control the parallelism level, you can also use the
        // common pool
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            customThreadPool.submit(() -> {
                IntStream.range(0, populationSize).parallel().forEach(i -> {
                    // Add the individual to the concurrent collection
                    Random rnd = threadSafeRandom.get();
                    pop.add(generateSmartIndividualGreedy(buffImg, colorDiffCutOutForGeneration * (rnd.nextDouble() + 0.5)));

                });
            }).get(); // Waiting for all tasks to complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customThreadPool.shutdown();
            System.out.println("created individuals");
        }

        return new ArrayList<>(pop);
    }

    private static SolutionRepresentation generateSmartIndividualRandom(BufferedImage buffImg, int populationLength) {
        Image img = loadImage(buffImg);
        Pixel[][] pixels = img.getPixels();
        int width = img.getWidth();
        int height = img.getHight();
        // track a list of options for pixels
        HashSet<Pixel> pixelOptions = new HashSet<Pixel>();
        ArrayList<Pixel> trackedPixelSolution = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p = pixels[i][j];
                pixelOptions.add(p);
                trackedPixelSolution.add(p);
            }
        }

        ArrayList<Pixel> solution = new ArrayList<Pixel>();
        Set<Integer> pixelKeysVisited = new HashSet<Integer>();

        while (!pixelOptions.isEmpty()) {
            // thread safe random
            Random rnd = threadSafeRandom.get();
            // select a random pixel to start a region
            List<Pixel> pixelOptionsTemp = new ArrayList<Pixel>(pixelOptions);
            Pixel currentPixel = pixelOptionsTemp.get(rnd.nextInt(pixelOptions.size()));
            pixelKeysVisited.add(currentPixel.getKey());
            pixelOptions.remove(currentPixel);
            // set it to connect to itsself (working backwards)
            currentPixel.setConnection(PossibleConnections.NONE);
            // search for a neighbour
            boolean search = true;
            // Maximum length of a "snake" of connected pixels
            int max = 15000;
            // max = Integer.MAX_VALUE;
            int j = 0;
            // keep track of neighbours
            ArrayList<Pixel> possibleNeighbours = new ArrayList<>();
            // keep track of the visited each loop/segment/snake
            Set<Integer> visitedThisLoop = new HashSet<>();
            visitedThisLoop.add(currentPixel.getKey());
            while (search) {
                // get and filter new neighbours
                List<Integer> neighbourKeys = currentPixel.getNeighbors();
                List<Pixel> neighbours = new ArrayList<Pixel>();
                for (Integer key : neighbourKeys) {
                    if (key != null) {
                        neighbours.add(trackedPixelSolution.get(key));
                    }
                }
                List<Pixel> newNeighbourOptions = neighbours.stream()
                        .filter(Objects::nonNull)
                        .filter(neighbour -> !possibleNeighbours.contains(neighbour))
                        .filter(neighbour -> !pixelKeysVisited.contains(neighbour.getKey()))
                        .collect(Collectors.toList());
                // add the new neighbours
                possibleNeighbours.addAll((newNeighbourOptions));
                // if no possibilities or "snake" length too long, stop the search and go to the
                // next random pixel to start a new "snake"
                if (possibleNeighbours.size() == 0 || j > max) {
                    search = false;
                } else {
                    // select the neighbour by random number
                    Pixel neighbour = possibleNeighbours.get(rnd.nextInt(possibleNeighbours.size()));
                    // get the location of an element in the new segment/snake that borders the
                    // selected neighbour
                    int nr = -1;
                    for (int z = 0; z < neighbour.neighbors.size(); z++) {
                        if (neighbour.neighbors.get(z) != null) {
                            if (visitedThisLoop.contains(neighbour.neighbors.get(z))) {
                                nr = z;
                                break;
                            }
                        }
                    }
                    // connect the neighbour to the current segment
                    neighbour.setConnection(PossibleConnections.values()[nr]);
                    // add neighbour to visited
                    pixelKeysVisited.add(neighbour.getKey());
                    visitedThisLoop.add(neighbour.getKey());
                    // remove as an option
                    possibleNeighbours.remove(neighbour);
                    pixelOptions.remove(neighbour);
                    // set current to neighbour
                    currentPixel = neighbour;
                    // increase the size of the "snake/segment"
                    j++;
                }
            }
        }
        // actually add the new pixels to the solution
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p = pixels[i][j];
                solution.add(p);
            }
        }

        return new SolutionRepresentation(solution, pixels[0].length);

    }

    private static SolutionRepresentation generateSmartIndividualGreedy(BufferedImage buffImg, double colorDiffCutOutForGeneration) {
        //todo Somehow prevent it from creating a lot of very small segments (or finding the right color diff at 222)
        Image img = loadImage(buffImg);
        Pixel[][] pixels = img.getPixels();
        int width = img.getWidth();
        int height = img.getHight();
        // track a list of options for pixels
        HashSet<Pixel> pixelOptions = new HashSet<Pixel>();
        ArrayList<Pixel> trackedPixelSolution = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p = pixels[i][j];
                pixelOptions.add(p);
                trackedPixelSolution.add(p);
            }
        }

        ArrayList<Pixel> solution = new ArrayList<Pixel>();
        Set<Integer> pixelKeysVisited = new HashSet<Integer>();

        while (!pixelOptions.isEmpty()) {
            // thread safe random
            Random rnd = threadSafeRandom.get();
            // select a random pixel to start a region
            List<Pixel> pixelOptionsTemp = new ArrayList<Pixel>(pixelOptions);
            Pixel currentPixel = pixelOptionsTemp.get(rnd.nextInt(pixelOptions.size()));
            pixelOptions.remove(currentPixel);
            // set it to connect to itsself (working backwards)
            currentPixel.setConnection(PossibleConnections.NONE);
            // search for a neighbour
            boolean search = true;
            // keep track of neighbours
            ArrayList<Object[]> possibleNeighbours = new ArrayList<>();
            // keep track of the visited each loop/segment/snake
            Set<Integer> visitedThisLoop = new HashSet<>();
            while (search) {
                // get and filter new neighbours
                List<Integer> neighbourKeys = currentPixel.getNeighbors().subList(0, 4);//only get 4dir neighbours for this???
                ArrayList<Object[]> neighbours = new ArrayList<Object[]>();
                for (Integer key : neighbourKeys) {
                    if (key != null) {
                        Pixel nb = trackedPixelSolution.get(key);
                        neighbours.add(new Object[]{nb,currentPixel, nb.getDistanceTo(currentPixel.getColor())});
                    }
                }
                List<Object[]> newNeighbourOptions = neighbours.stream()
                        // Filter out if the first Pixel is null
                        .filter(n -> Objects.nonNull(n[0]))
                        // Ensure the Pixel does not exist in possibleNeighbours
                        .filter(n -> possibleNeighbours.stream()
                                .noneMatch(p -> ((Pixel) p[0]).equals(n[0])))
                        .filter(n -> !pixelKeysVisited.contains(((Pixel) n[0]).getKey()))
                        .collect(Collectors.toList());
                // add the new neighbours
                possibleNeighbours.addAll((newNeighbourOptions));
                // if no possibilities or "snake" length too long, stop the search and go to the
                // next random pixel to start a new "snake"
                if (possibleNeighbours.size() == 0) {
                    search = false;
                } else {
                    // sort the neighbourOptions by [2] value
                    Collections.sort(possibleNeighbours, new Comparator<Object[]>() {
                        @Override
                        public int compare(Object[] o1, Object[] o2) {
                            Double value1 = (Double) o1[2];
                            Double value2 = (Double) o2[2];
                            return value1.compareTo(value2);
                        }
                    });
                    //get the neighbour
                    Object[] neighbourObject = possibleNeighbours.get(0);
                    //stop if the best distance is too big
                    if ((Double) neighbourObject[2] > colorDiffCutOutForGeneration){
                        search = false;
                    } else {
                        Pixel neighbour = (Pixel) neighbourObject[0];
                        //get the pixel it should be connected to
                        Pixel toConnectToNeighbour = (Pixel) possibleNeighbours.get(0)[1];
                        //get the direction number for toConnectToNeighbour
                        int nr = neighbour.getNeighbors().indexOf(toConnectToNeighbour.getKey());
                        // connect the neighbour to the current segment
                        neighbour.setConnection(PossibleConnections.values()[nr]);
                        // add neighbour to visited
                        pixelKeysVisited.add(neighbour.getKey());
                        //remove as an option
                        possibleNeighbours.remove(neighbourObject);
                        pixelOptions.remove(neighbour);
                        // set current to neighbour
                        currentPixel = neighbour;
                    }
                }
            }
        }
        // actually add the new pixels to the solution
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel p = pixels[i][j];
                solution.add(p);
            }
        }

        return new SolutionRepresentation(solution, pixels[0].length);

    }

    public static ArrayList<SolutionRepresentation> generatePopulation(int populationSize, BufferedImage img) {
        // could change to threading aswell
        ArrayList<SolutionRepresentation> population = new ArrayList<SolutionRepresentation>();

        for (int i = 0; i < populationSize; i++) {
            population.add(generateSolutionRand(img));
        }

        return population;
    }

    private static SolutionRepresentation generateSolutionRand(BufferedImage buffImg) {

        ArrayList<Pixel> solution = new ArrayList<Pixel>();
        Image img = loadImage(buffImg);
        // flatten img
        Pixel[][] pixels = img.getPixels();
        for (int i = 0; i < img.getHight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                Pixel ps = pixels[i][j];
                Pixel p = new Pixel(ps.getKey(), ps.getColor(), ps.getNeighbors());
                // set connection of the pixel to a random one
                p.setConnection(
                        PossibleConnections.values()[(int) (Math.random() *
                                9)]);
                // p.setConnection(PossibleConnections.RIGHT);
                solution.add(p);
            }
        }

        return new SolutionRepresentation(solution, img.getWidth());
    }

    public static Image loadImage(BufferedImage imgBuf) {

        // imgBuf = ImageIO.read(new File("test.png"));
        // System.out.println("Found image with width: " + imgBuf.getWidth() + " and
        // height: " + imgBuf.getHeight());
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

        // System.out.println("Image loaded successfully");
        return img;
    }

}
