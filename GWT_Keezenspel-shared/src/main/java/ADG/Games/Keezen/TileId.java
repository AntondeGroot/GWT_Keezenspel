package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;


public class TileId implements IsSerializable {
    private String playerId;
    private int tileNr;

    public TileId(String playerId, int tileNr) {
        this.playerId = playerId;
        this.tileNr = tileNr;
    }

    public TileId() {
    }

    public TileId(TileId other){
        this.playerId = other.playerId;
        this.tileNr = other.tileNr;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
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
        return tileNr == tileId.tileNr && Objects.equals(playerId, tileId.playerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, tileNr);
    }
}
