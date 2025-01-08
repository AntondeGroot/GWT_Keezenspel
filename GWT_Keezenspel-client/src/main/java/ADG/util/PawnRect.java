package ADG.util;

import ADG.Point;

public class PawnRect {


    public static double[] getRect(Point point){
        int desiredWidth = 40;
        int desiredHeight = 40;
        return new double[]{point.getX()-desiredWidth/2 , point.getY()-desiredHeight/2-15, desiredWidth,desiredHeight};
    }
}
