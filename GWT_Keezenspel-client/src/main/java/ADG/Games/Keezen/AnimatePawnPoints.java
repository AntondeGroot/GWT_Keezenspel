package ADG.Games.Keezen;

import ADG.Games.Keezen.Player.Pawn;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.LinkedList;

public class AnimatePawnPoints {
    private final Pawn pawn;
    private final LinkedList<Point> points = new LinkedList<>();
    private double totalPathLength = 0;

    public AnimatePawnPoints(Pawn pawn, LinkedList<TileId> tileIdList) {
        this.pawn = pawn;
        ArrayList<Point> tempResult = new ArrayList<>();

        Point pointTo = new Point(0,0);
        for (int i = 0; i < tileIdList.size()-1 ; i++) {
            Point pointFrom = convertTileIdToPoint(tileIdList.get(i));
            pointTo = convertTileIdToPoint(tileIdList.get(i+1));

            tempResult.add(pointFrom);
        }
        tempResult.add(pointTo);
        GWT.log("positions for drawing a line is: "+tempResult);
        points.addAll(tempResult);

        calculatePathLength(points);
    }

    public double getTotalPathLength(){
        return totalPathLength;
    }

    private Point convertTileIdToPoint(TileId tileId){
        for (TileMapping mapping : Board.getTiles()) {
            if(mapping.getTileId().equals(tileId)){
                return mapping.getPosition();
            }
        }
        return new Point(0,0);
    }

    public Pawn getPawn() {
        return pawn;
    }

    public LinkedList<Point> getPoints() {
        return new LinkedList<>(points);
    }

    private void calculatePathLength(LinkedList<Point> points){
        if(points.isEmpty()){return;}

        Point startPoint = points.get(0);
        double x0 = startPoint.getX();
        double y0 = startPoint.getY();

        for(Point p: points){
            double dx = p.getX() - x0;
            double dy = p.getY() - y0;

            totalPathLength += Math.sqrt(dx*dx + dy*dy);
            x0 = p.getX();
            y0 = p.getY();
        }
    }

    @Override
    public String toString() {
        return "PawnAnimationMapping{" +
                "pawn=" + pawn +
                ", points=" + points + '}';
    }
}
