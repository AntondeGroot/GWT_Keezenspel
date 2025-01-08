package ADG;

public class PlayerColors {
    private static final String[] colors = {
            "#A52A2A", "#0000A5", "#008000", "#A5A500",
            "#6A5ACD", "#FF8C00", "#008B8B", "#8B008B"
    };

    private static double lightenFactor = 0.5; // 20% lighter

    public static int[] lightenColor(String hex, double factor) {
        int[] rgb = hexToRgb(hex);
        for (int i = 0; i < 3; i++) {
            rgb[i] = (int) (rgb[i] + (255 - rgb[i]) * factor);
            if (rgb[i] > 255) rgb[i] = 255;
        }
        return rgb;
    }

    public static String getHexColor(int userId){
            return colors[userId];
        }

    private static int[] hexToRgb(String hex) {
        hex = hex.substring(1); // Remove the leading #
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    public static int[] getRGBColor(int userId){
        return lightenColor(getHexColor(userId), lightenFactor);
    }
}
