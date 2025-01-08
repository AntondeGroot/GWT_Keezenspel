package ADG;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;


public class TileId implements IsSerializable {
    private int playerId;
    private int tileNr;

    public TileId(int playerId, int tileNr) {
        this.playerId = playerId;
        this.tileNr = tileNr;
    }

    public TileId() {
    }

    public TileId(TileId other){
        this.playerId = other.playerId;
        this.tileNr = other.tileNr;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileId tileId = (TileId) o;
        return playerId == tileId.playerId && tileNr == tileId.tileNr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, tileNr);
    }
}
