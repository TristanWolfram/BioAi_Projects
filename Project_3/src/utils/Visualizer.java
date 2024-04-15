package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class Visualizer {

    public static void visualizeSolution(SolutionRepresentation solution, int i) {
        Image colorWithBorders = visualizeSegmentsColor(solution);
        Image blackWhiteWithBorders = visualizeSegmentsBlackWhite(solution);
        String path = "Project_3/Project 3 evaluator/Project 3 evaluator/student_segments/results/";
        saveImage(blackWhiteWithBorders, "test_black_white_" + i, path);
        path = "Project_3/Project 3 evaluator/Project 3 evaluator/student_segments/coloredResults/";
        saveImage(colorWithBorders, "test_color_" + i, path);
    }

    public static void visualizeFrontier(List<SolutionRepresentation> solutions) {
        int i = 0;
        DeleteFiles("Project_3/Project 3 evaluator/Project 3 evaluator/student_segments/results/");
        DeleteFiles("Project_3/Project 3 evaluator/Project 3 evaluator/student_segments/coloredResults/");
        for (SolutionRepresentation solution : solutions) {
            visualizeSolution(solution, i);
            i++;
        };
    }

    public static Image visualizeSegmentsColor(SolutionRepresentation solution) {
        ArrayList<Segment> segments = solution.getSegments();
        HashSet<Segment> coloredSegments = new HashSet<>();
        for (Segment segment : segments) {
            colorSegmentBorders(segment, solution, new RGBRepresentation(0, 255, 0), coloredSegments);
            coloredSegments.add(segment);
        }
        Image img = SoultionRepToImage(solution);
        return img;
    }

    public static Image visualizeSegmentsBlackWhite(SolutionRepresentation solution) {
        for (Pixel pixel : solution.getSolution()) {
            pixel.setColor(new RGBRepresentation(255, 255, 255));
        }

        ArrayList<Segment> segments = solution.getSegments();
        HashSet<Segment> coloredSegments = new HashSet<>();
        for (Segment segment : segments) {
            colorSegmentBorders(segment, solution, new RGBRepresentation(0, 0, 0), coloredSegments);
            coloredSegments.add(segment);
        }
        Image img = SoultionRepToImage(solution);
        return img;
    }

    public static void viusalizeSegments(SolutionRepresentation solution) {
        Image coloredSegments = visualizeImageFilling(solution);
        String path = "Project_3/Project 3 evaluator/Project 3 evaluator/student_segments/other/";
        saveImage(coloredSegments, "coloredSegments", path);
    }

    public static Image visualizeImageFilling(SolutionRepresentation solution) {
        for (Pixel pixel : solution.getSolution()) {
            pixel.setColor(new RGBRepresentation(255, 255, 255));
        }

        ArrayList<Segment> segments = solution.getSegments();
        for (Segment segment : segments) {
            // get a random color
            int r = (int) (Math.random() * 256);
            int g = (int) (Math.random() * 256);
            int b = (int) (Math.random() * 256);

            colorSegment(segment, new RGBRepresentation(r, g, b));
        }

        Image img = SoultionRepToImage(solution);
        return img;
    }

    public static void colorSegmentBorders(Segment segment, SolutionRepresentation solution, RGBRepresentation color,
            HashSet<Segment> coloredSegments) {

        for (Pixel pixel : segment.getSegment()) {
            ArrayList<Integer> neighbourKeys = new ArrayList<>(pixel.getNeighbors().subList(0, 4));
            for (Integer key : neighbourKeys) {
                if (key != null) {
                    Pixel neighbour = solution.getSolution().get(key);
                    if (neighbour != null && !segment.contains(neighbour)
                            && !checkIfPixelIsInGivenSegments(neighbour, coloredSegments)) {
                        pixel.setColor(color);
                        break;
                    }
                } else {
                    pixel.setColor(color);
                    break;
                }
            }
        }

    }

    public static void colorSegment(Segment segment, RGBRepresentation color) {
        for (Pixel pixel : segment.getSegment()) {
            pixel.setColor(color);
        }
    }

    private static boolean checkIfPixelIsInGivenSegments(Pixel pixel, HashSet<Segment> coloredSegments) {
        for (Segment segment : coloredSegments) {
            if (segment.contains(pixel)) {
                return true;
            }
        }
        return false;
    }

    public static Image SoultionRepToImage(SolutionRepresentation solution) {
        int imgWidth = solution.getImageWidth();
        int imgHight = solution.getSolution().size() / imgWidth;
        System.out.println(imgWidth + " " + imgHight);
        Image img = new Image(imgHight, imgWidth);
        ArrayList<Pixel> pixels = solution.getSolution();
        for (int y = 0; y < imgHight; y++) {
            for (int x = 0; x < imgWidth; x++) {
                Pixel p = pixels.get(y * imgWidth + x);
                Pixel p_new = new Pixel(p.getKey(), p.getColor(), p.getNeighbors());
                img.setPixel(y, x, p_new);
            }
        }
        return img;
    }

    public static SolutionRepresentation ImageToSolutionRep(Image img) {
        int imgWidth = img.getWidth();
        ArrayList<Pixel> pixels = new ArrayList<>();
        for (int i = 0; i < img.getHight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                Pixel p = img.getPixel(j, i);
                Pixel p_new = new Pixel(p.getKey(), p.getColor(), p.getNeighbors());
                pixels.add(p_new);
            }
        }
        return new SolutionRepresentation(pixels, imgWidth);
    }

    public static void saveImage(Image img, String name, String path) {
        BufferedImage bufferedImage = img.toBufferedImage();
        File outputfile = new File(
                path + name + ".jpg");
        try {
            ImageIO.write(bufferedImage, "jpg", outputfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void DeleteFiles (String delPath){
        Path directoryPath = Paths.get(delPath);
        try {
            // Delete all files in the directory
            Files.walk(directoryPath)
                 .filter(Files::isRegularFile)  // Filter to only include regular files
                 .forEach(path -> {
                     try {
                         Files.delete(path);  // Delete each file
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
