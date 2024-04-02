package utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Visualizer {

    public static void visualizeSolution(SolutionRepresentation solution) {
        Image colorWithBorders = visualizeSegmentsColor(solution);
        colorWithBorders.show();
        Image blackWhiteWithBorders = visualizeSegmentsBlackWhite(solution);
        blackWhiteWithBorders.show();
        saveImage(blackWhiteWithBorders, "test");
    }

    public static Image visualizeSegmentsColor(SolutionRepresentation solution) {
        ArrayList<Segment> segments = solution.getSegments();
        for (Segment segment : segments) {
            colorSegmentBorders(segment, solution, new RGBRepresentation(0, 255, 0));
        }
        Image img = SoultionRepToImage(solution);
        return img;
    }

    public static Image visualizeSegmentsBlackWhite(SolutionRepresentation solution) {
        for (Pixel pixel : solution.getSolution()) {
            pixel.setColor(new RGBRepresentation(255, 255, 255));
        }

        ArrayList<Segment> segments = solution.getSegments();
        for (Segment segment : segments) {
            colorSegmentBorders(segment, solution, new RGBRepresentation(0, 0, 0));
        }
        Image img = SoultionRepToImage(solution);
        return img;
    }

    public static void colorSegmentBorders(Segment segment, SolutionRepresentation solution, RGBRepresentation color) {

        for (Pixel pixel : segment.getSegment()) {
            ArrayList<Integer> neighbourKeys = pixel.getNeighbors();
            for (Integer key : neighbourKeys) {
                if (key != null) {
                    Pixel neighbour = solution.getSolution().get(key);
                    if (neighbour != null && !segment.contains(neighbour)) {
                        pixel.setColor(color);
                    }
                }
            }
        }

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

    public static void saveImage(Image img, String name) {
        BufferedImage bufferedImage = img.toBufferedImage();
        File outputfile = new File(
                "Project_3/Project 3 evaluator/Project 3 evaluator/student_segments/" + name + ".jpg");
        try {
            ImageIO.write(bufferedImage, "jpg", outputfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
