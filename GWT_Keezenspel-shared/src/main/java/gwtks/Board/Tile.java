package gwtks.Board;

import java.util.Random;

public class Tile {
    private final long id;
    private final TileType tileType;
    private final long playerId;
    private long previousTileId;
    private long nextTileId;
    private long nextFinishTileId;

    public Tile(long playerId, TileType tileType) {
        id = new Random().nextLong();
        this.playerId = playerId;
        this.tileType = tileType;
    }

    public long getId() {
        return id;
    }

    public long getPreviousTileId() {
        return previousTileId;
    }

    public long getNextTileId() {
        return nextTileId;
    }

    public long getNextFinishTileId() {
        return nextFinishTileId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPreviousTileId(long previousTileId) {
        this.previousTileId = previousTileId;
    }

    public void connect(Tile other) {
        if (other.getType() == TileType.FINISH) {
            this.nextFinishTileId = other.getId();
            other.setPreviousTileId(this.id);
        } else {
            this.nextTileId = other.getId();
            other.setPreviousTileId(this.id);
        }
    }

    public TileType getType() {
        return this.tileType;
    }

    @Override
    public String toString() {
        return "\nTile [id: " + shortenId(id) + "\nplayerId:" + playerId + "\nTileType:" + tileType + "\npreviousId: "
                + shortenId(previousTileId) + "\nnextTileId: " + shortenId(nextTileId) + "\nnextHomeId: "
                + shortenId(nextFinishTileId) + "]\n";
    }

    private String shortenId(long number) {
        if (number == 0) {
            return "0";
        }
        String numberString = String.valueOf(number);
        return numberString.substring(numberString.length() - 3);
    }

}