package gwtks;

import java.util.ArrayDeque;

public class PawnAnimationMapping {
    private Pawn pawn;
    private ArrayDeque<Point> points;

    public PawnAnimationMapping(Pawn pawn, TileId tileIdFrom, TileId tileIdTo) {
        this.pawn = pawn;
        Point pointFrom = new Point(0,0);
        Point pointTo = new Point(0,0);

        for (TileMapping mapping : Board.getTiles()) {
            if(mapping.getTileId().equals(tileIdFrom)){
                pointFrom = mapping.getPosition();
            }
        }
        for (TileMapping mapping : Board.getTiles()) {
            if(mapping.getTileId().equals(tileIdTo)){
                pointTo = mapping.getPosition();
            }
        }

        points = drawLine(pointFrom,pointTo);
    }

    public ArrayDeque<Point> drawLine(Point point1, Point point2) {
        ArrayDeque<Point> points = new ArrayDeque<>();

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

    public Pawn getPawn() {
        return pawn;
    }

    public ArrayDeque<Point> getPoints() {
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
