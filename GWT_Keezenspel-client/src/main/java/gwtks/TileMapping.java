package gwtks;

public class TileMapping {
    private int playerId;
    private int tileNr;
    private Point position;

    public TileMapping(int playerId, int tileNr, Point position) {
        this.playerId = playerId;
        this.tileNr = tileNr;
        this.position = position;
    }

    public int getPlayerId() {
        return playerId;
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
                "playerId='" + playerId + '\'' +
                ", tileNr=" + tileNr +
                ", position=" + position +
                '}';
    }
}