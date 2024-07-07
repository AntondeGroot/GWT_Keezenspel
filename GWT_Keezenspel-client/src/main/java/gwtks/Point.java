package gwtks;

public class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = roundedValue(x);
        this.y = roundedValue(y);
    }

    public Point(Point p){
        if(p == null){
            p = new Point(0,0);
        }
        this.x = roundedValue(p.x);
        this.y = roundedValue(p.y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x +", " + y +')';
    }

    public Point rotate(Point center, double angleDegrees) {
        // Convert the angle from degrees to radians
        double angleRadians = Math.toRadians(angleDegrees);

        // Translate point back to origin
        double translatedX = this.x - center.x;
        double translatedY = this.y - center.y;

        // Apply rotation
        double rotatedX = translatedX * Math.cos(angleRadians) - translatedY * Math.sin(angleRadians);
        double rotatedY = translatedX * Math.sin(angleRadians) + translatedY * Math.cos(angleRadians);

        // Translate point back to its original location
        double finalX = rotatedX + center.x;
        double finalY = rotatedY + center.y;

        return new Point(finalX, finalY);
    }

    public double roundedValue(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
