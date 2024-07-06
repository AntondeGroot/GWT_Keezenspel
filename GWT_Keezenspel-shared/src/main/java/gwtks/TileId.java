package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;


public class TileId implements IsSerializable {
    private int playerId;
    private int tileNr;

    public TileId(int playerId, int tileNr) {
        this.playerId = playerId;
        this.tileNr = tileNr;
    }

    public TileId() {
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getTileNr() {
        return tileNr;
    }

    public void setTileNr(int tileNr) {
        this.tileNr = tileNr;
    }

    @Override
    public String toString() {
        return "TileId{" + playerId +"," + tileNr + '}';
    }
}
