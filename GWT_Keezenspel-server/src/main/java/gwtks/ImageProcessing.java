package gwtks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;

import java.awt.Color;

public class ImageProcessing {
    public static void create(int colorId) {
        // Use the correct path to access the image file in the resources directory
        String resourcePath = "/public/pawn.png"; // Relative to the resources folder
        int[] RGB = PlayerColors.getRGBColor(colorId);
        try {
            // Load the image from the resources folder
            URL resourceUrl = ImageProcessing.class.getResource(resourcePath);
            if (resourceUrl == null) {
                System.err.println("Image file not found at " + resourcePath);
                return;
            }

            // Read the image file
            File inputFile = new File(resourceUrl.toURI());
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                System.err.println("Failed to load image from " + resourcePath);
                return;
            }

            // Ensure the image is in the correct format
            BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

            // Process the image
            int width = image.getWidth();
            int height = image.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    Color color = new Color(pixel, true);

                    // Check if the pixel is white
                    if (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255) {
                        // Replace with red
                        Color newColor = new Color(RGB[0], RGB[1], RGB[2], color.getAlpha()); // Red color with original alpha
                        processedImage.setRGB(x, y, newColor.getRGB());
                    } else {
                        processedImage.setRGB(x, y, pixel);
                    }
                }
            }

            // Prepare the output file path
            String outputImagePath = inputFile.getParent() + File.separator + "pawn"+colorId+".png";
            File outputFile = new File(outputImagePath);

            // Save the modified image
            ImageIO.write(processedImage, "png", outputFile);

            System.out.println("Image processing completed. The output image is saved at: " + outputImagePath);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
