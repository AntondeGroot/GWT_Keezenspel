package ADG;

import ADG.TileId;

public class TileMapping {
    private TileId tileId;
    private Point position;

    public TileMapping(int playerId, int tileNr, Point position) {
        this.tileId = new TileId(playerId, tileNr);
        this.position = position;
    }

    public int getPlayerId() {
        return tileId.getPlayerId();
    }

    public int getTileNr() {
        return tileId.getTileNr();
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public TileId getTileId() {
        return tileId;
    }

    @Override
    public String toString() {
        return "Mapping{" + tileId + " -> [" + position + "]}";
    }
}