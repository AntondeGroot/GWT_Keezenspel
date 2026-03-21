package ADG.Games.Keezen;

import ADG.Games.Keezen.Player.PlayerColors;
import ADG.Log;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class ImageProcessing {
  public static void create(int colorId) {
    create(colorId, System.getProperty("user.dir") + "/public");
  }

  public static void create(int colorId, String outputDirPath) {
    String resourcePath = "/public/pawn.png";
    int[] rgb = PlayerColors.getRGBColor(colorId);

    File outputDir = new File(outputDirPath);
    outputDir.mkdirs();
    File outputFile = new File(outputDir, "pawn" + colorId + ".png");

    if (outputFile.exists()) {
      Log.info("Skipping image creation: " + outputFile.getName() + " already exists.");
      return;
    }

    try (InputStream is = ImageProcessing.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        System.err.println("Image file not found at " + resourcePath);
        return;
      }

      BufferedImage image = ImageIO.read(is);
      if (image == null) {
        System.err.println("Failed to load image from " + resourcePath);
        return;
      }

      BufferedImage processedImage =
          new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

      int width = image.getWidth();
      int height = image.getHeight();

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int pixel = image.getRGB(x, y);
          Color color = new Color(pixel, true);

          if (color.getRed() == 255 && color.getGreen() == 255 && color.getBlue() == 255) {
            Color newColor = new Color(rgb[0], rgb[1], rgb[2], color.getAlpha());
            processedImage.setRGB(x, y, newColor.getRGB());
          } else {
            processedImage.setRGB(x, y, pixel);
          }
        }
      }

      ImageIO.write(processedImage, "png", outputFile);
      Log.info("Image processing completed. Output: " + outputFile.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
