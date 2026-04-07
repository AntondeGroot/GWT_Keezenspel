package adg.keezen.player;

public class PawnHighlightColors {

  public static final String RED   = "#ef5350";
  public static final String GREEN = "#66bb6a";
  public static final String BLUE  = "#1e90ff";

  /** Returns the highlight color to use for pawn 1 (prefers red, falls back to blue). */
  public static String forPawn1(String pawnColor) {
    return colorsClash(pawnColor, RED) ? BLUE : RED;
  }

  /** Returns the highlight color to use for pawn 2 (prefers green, falls back to blue). */
  public static String forPawn2(String pawnColor) {
    return colorsClash(pawnColor, GREEN) ? BLUE : GREEN;
  }

  public static boolean colorsClash(String pawnColor, String highlightColor) {
    if (pawnColor == null) return false;
    double hueDiff = Math.abs(computeHue(hexToRgb(pawnColor))
        - computeHue(hexToRgb(highlightColor)));
    if (hueDiff > 180) hueDiff = 360 - hueDiff;
    return hueDiff < 40;
  }

  public static double computeHue(int[] rgb) {
    double r = rgb[0] / 255.0;
    double g = rgb[1] / 255.0;
    double b = rgb[2] / 255.0;
    double max = Math.max(r, Math.max(g, b));
    double min = Math.min(r, Math.min(g, b));
    double delta = max - min;
    if (delta == 0) return 0;
    double hue;
    if (max == r)      hue = 60 * (((g - b) / delta) % 6);
    else if (max == g) hue = 60 * ((b - r) / delta + 2);
    else               hue = 60 * ((r - g) / delta + 4);
    return hue < 0 ? hue + 360 : hue;
  }

  private static int[] hexToRgb(String hex) {
    hex = hex.substring(1);
    return new int[]{
        Integer.parseInt(hex.substring(0, 2), 16),
        Integer.parseInt(hex.substring(2, 4), 16),
        Integer.parseInt(hex.substring(4, 6), 16)
    };
  }
}