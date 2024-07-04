package gwtks;

public class TileMapping {
    private int userId;
    private int tileNr;
    private Point position;

    public TileMapping(int userId, int tileNr, Point position) {
        this.userId = userId;
        this.tileNr = tileNr;
        this.position = position;
    }

    public int getUserId() {
        return userId;
    }

    public int getTileNr() {
        return tileNr;
    }

    public Point getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "userId='" + userId + '\'' +
                ", tileNr=" + tileNr +
                ", position=" + position +
                '}';
    }
}