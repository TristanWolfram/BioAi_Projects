import utils.Image;
import utils.Pixel;
import utils.RGBRepresentation;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.image.BufferedImage;
import java.io.File;

public class MOOP {
    public static void main(String[] args) {
        System.out.println("Hello, MOOP!");

        RGBRepresentation rgb = new RGBRepresentation(255, 255, 255);
        System.out.println("Color: " + rgb);

        Pixel pixel = new Pixel(0, rgb);
        System.out.println("Key: " + pixel.getKey() + pixel.getNeighbors());

        JFrame frame = new JFrame();
        frame.setTitle("Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String imgPath = "/../img_data_set/BSDS300-images/BSDS300/images/train/370036.jpg";

        Image img = loadImage(imgPath);
        img.printImage();

        BufferedImage image = img.toBufferedImage();

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static Image loadImage(String path) {

        try {
            String currentDir = new java.io.File(".").getCanonicalPath();
            System.out.println("Current path: " + currentDir);

            BufferedImage imgBuf = ImageIO.read(new File(currentDir + path));
            System.out.println("Found image with width: " + imgBuf.getWidth() + " and height: " + imgBuf.getHeight());
            Image img = new Image(imgBuf.getHeight(), imgBuf.getWidth());

            int pixelID = 0;
            int hight = imgBuf.getHeight();
            int width = imgBuf.getWidth();

            for (int y = 0; y < hight; y++) {
                for (int x = 0; x < width; x++) {
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
            System.out.println("Image loaded successfully");
            return img;
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        System.out.println("Error: Could not load image");
        return null;
    }
}
