package ADG;

import com.google.gwt.core.client.GWT;
import ADG.Pawn;
import ADG.TileId;

import java.util.ArrayList;
import java.util.LinkedList;

public class PawnAnimationMapping {
    private Pawn pawn;
    private LinkedList<Point> points = new LinkedList<>();
    private boolean animateLast;

    public PawnAnimationMapping(Pawn pawn, LinkedList<TileId> tileIdList, boolean animateLast) {
        this.pawn = pawn;
        this.animateLast = animateLast;
        ArrayList<Point> tempResult = new ArrayList<Point>();

        for (int i = 0; i < tileIdList.size()-1 ; i++) {
            Point pointFrom = convertTileIdToPoint(tileIdList.get(i));
            Point pointTo = convertTileIdToPoint(tileIdList.get(i+1));

            tempResult.addAll(drawLine(pointFrom,pointTo));
        }
        GWT.log("positions for drawing a line is: "+tempResult);
        points.addAll(tempResult);
    }

    private Point convertTileIdToPoint(TileId tileId){
        for (TileMapping mapping : Board.getTiles()) {
            if(mapping.getTileId().equals(tileId)){
                return mapping.getPosition();
            }
        }
        return new Point(0,0);
    }

    public LinkedList<Point> drawLine(Point point1, Point point2) {
        LinkedList<Point> points = new LinkedList<>();

        // Calculate the distance between start and end points
        double distance = Math.sqrt(Math.pow(point2.getX() - point1.getX(), 2) + Math.pow(point2.getY() - point1.getY(), 2));

        // Calculate the unit direction vector
        double directionX = (point2.getX() - point1.getX()) / distance;
        double directionY = (point2.getY() - point1.getY()) / distance;

        // Generate points along the direction vector at intervals of 1 unit distance
        double currentX = point1.getX();
        double currentY = point1.getY();
        points.add(new Point(currentX, currentY));

        while (Math.sqrt(Math.pow(currentX - point1.getX(), 2) + Math.pow(currentY - point1.getY(), 2)) < distance) {
            currentX += directionX;
            currentY += directionY;
            points.add(new Point(currentX, currentY));
        }

        // Ensure the end point is included in the list
        points.add(point2);

        return points;
    }

    public boolean isAnimateLast() {
        return animateLast;
    }

    public void setAnimateLast(boolean animateLast) {
        this.animateLast = animateLast;
    }

    public Pawn getPawn() {
        return pawn;
    }

    public LinkedList<Point> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return "PawnAnimationMapping{" +
                "pawn=" + pawn +
                ", points=" + points.getFirst() + " - " + points.getLast() +
                '}';
    }
}
