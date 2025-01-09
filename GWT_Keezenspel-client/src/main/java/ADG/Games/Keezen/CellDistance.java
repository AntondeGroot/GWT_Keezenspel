package ADG.Games.Keezen;

import com.google.gwt.core.client.GWT;

public class CellDistance {

    public static double getCellDistance(int nrPlayers, double boardSize){
        // length from center to bottom of first boardsection is equal to:
        // a board section is 4D wide and 6D high, we use half the width 2D
        // the board section is offset from the middle by a certain distance X
        // this distance is dependent on the number of players: 2 -> x=0 so that two boards are directly joined
        // the more players the larger the offset becomes, but this grows slower and slower

        // 2 * celldistance * Math.tan(Math.toRadians(90 - 180 / nrplayers)) + 6*celldistance;
        double padding = 50;
        double availableRadius = boardSize / 2 - padding;

        double y = 2 * Math.tan(Math.toRadians(90 - 180.0 / nrPlayers));

        return availableRadius / (6 + y);
    }

    public static Point getStartPoint(int nrPlayers, double boardSize){
        double padding = 50;

        double y = 2 * Math.tan(Math.toRadians(90 - 180.0 / nrPlayers));

        Point midPoint = new Point(boardSize / 2, boardSize / 2);
        Point startPoint =  new Point(midPoint.getX() + 2*getCellDistance(nrPlayers, boardSize), midPoint.getY() + y*getCellDistance(nrPlayers, boardSize));
        //midPoint.getY() + y);
        GWT.log("y value = "+y);

        return startPoint;
    }
}
