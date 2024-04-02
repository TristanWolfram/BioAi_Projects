package utils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Image {
    private final int hight;
    private final int width;
    private final Pixel[][] pixels;

    public Image(int hight, int width) {
        this.hight = hight;
        this.width = width;
        this.pixels = new Pixel[hight][width];
    }

    public int getHight() {
        return hight;
    }

    public int getWidth() {
        return width;
    }

    public Pixel getPixel(int x, int y) {
        return pixels[x][y];
    }

    public void setPixel(int x, int y, Pixel pixel) {
        pixels[x][y] = pixel;
    }

    public Pixel[][] getPixels() {
        return pixels;
    }

    public void generateNeighbors() {
        int[][] neighborPositions = {
                { 0, 1 }, // East
                { 0, -1 }, // West
                { -1, 0 }, // North
                { 1, 0 }, // South
                { -1, 1 }, // North-East
                { 1, 1 }, // South-East
                { -1, -1 }, // North-West
                { 1, -1 } // South-West
        };

        for (int y = 0; y < hight; y++) {
            for (int x = 0; x < width; x++) {
                ArrayList<Integer> neighbors = new ArrayList<>();

                for (int[] position : neighborPositions) {
                    int neighborY = y + position[0];
                    int neighborX = x + position[1];

                    if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < hight) {
                        Pixel neighbor = pixels[neighborY][neighborX];
                        neighbors.add(neighbor.getKey());
                    } else {
                        neighbors.add(null);
                    }
                }

                pixels[y][x].neighbors = neighbors;
            }
        }
    }

    public void printImage() {
        System.out.println("Image with: " + hight + " hight and " + width + " width");
    }

    public void show() {
        JFrame frame = new JFrame();
        frame.setTitle("Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BufferedImage image = toBufferedImage();

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public BufferedImage toBufferedImage() {

        BufferedImage img = new BufferedImage(width, hight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < hight; y++) {
            for (int x = 0; x < width; x++) {
                Pixel p = pixels[y][x];
                RGBRepresentation color = p.getColor();
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                int rgbValue = (red << 16) | (green << 8) | blue;
                img.setRGB(x, y, rgbValue);
            }
        }

        return img;
    }
}
