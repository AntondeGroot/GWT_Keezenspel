package ADG.util;

public class PlayerColors {
  private static final String[] colors = {
    "#A52A2A", "#0000A5", "#008000", "#A5A500",
    "#6A5ACD", "#FF8C00", "#008B8B", "#8B008B"
  };

  public static String getPlayerColor(int i) {
    return colors[i];
  }
}
