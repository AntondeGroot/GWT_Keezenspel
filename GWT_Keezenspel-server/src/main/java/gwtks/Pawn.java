package gwtks;

public class Pawn {
    private int playerId;
    private TileId currentPosition;
    private TileId nestTileId;

    public Pawn(int playerId, int nestTileId) {
        this.playerId = playerId;
        this.currentPosition = new TileId(playerId, nestTileId);
        this.nestTileId = new TileId(playerId, nestTileId);
    }
}
