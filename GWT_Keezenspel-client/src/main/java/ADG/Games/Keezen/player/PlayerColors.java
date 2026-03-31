package ADG.Games.Keezen.player;

public class PlayerColors {
  private static final String[] colors = {
    "#A52A2A", "#0000A5", "#008000", "#A5A500",
    "#6A5ACD", "#FF8C00", "#008B8B", "#8B008B"
  };

  public static String getHexColor(int userId) {
    return colors[userId];
  }

  public static int[] hexToRgb(String hex) {
    hex = hex.substring(1); // Remove the leading #
    int r = Integer.parseInt(hex.substring(0, 2), 16);
    int g = Integer.parseInt(hex.substring(2, 4), 16);
    int b = Integer.parseInt(hex.substring(4, 6), 16);
    return new int[] {r, g, b};
  }

  public static String rgbToHex(int[] rgb) {
    StringBuilder hex = new StringBuilder("#");
    for (int value : rgb) {
      String hexValue = Integer.toHexString(value);
      if (hexValue.length() == 1) {
        hex.append("0");
      }
      hex.append(hexValue);
    }
    return hex.toString();
  }

  public static int[] darkenColor(int[] rgb) {
    for (int i = 0; i < 3; i++) {
      rgb[i] = (int) (rgb[i] * 0.6);
    }
    return rgb;
  }

  public static int[] lightenColor(int[] rgb) {
    for (int i = 0; i < 3; i++) {
      rgb[i] = (int) (rgb[i] * 1.55);
      if (rgb[i] >= 255) {
        rgb[i] = 254;
      }
    }
    return rgb;
  }
}