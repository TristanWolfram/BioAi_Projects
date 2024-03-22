package utils;

import java.awt.image.BufferedImage;

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

    public void printImage() {
        System.out.println("Image with: " + hight + " hight and " + width + " width");
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
