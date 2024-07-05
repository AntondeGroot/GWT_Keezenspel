package gwtks;

import java.io.Serializable;
import java.util.List;

public class Players implements Serializable {
    private static final String[] colors = {
            "#A52A2A", "#0000A5", "#008000", "#A5A500",
            "#6A5ACD", "#FF8C00", "#008B8B", "#8B008B"
    };

    public static String getColor(int userId){
        return colors[userId];
    }
}
