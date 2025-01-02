package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Objects;

public class Pawn implements IsSerializable {
    private PawnId pawnId;
    private TileId currentTileId;
    private TileId nestTileId;
    private int playerId;

    public Pawn() {
    }

    public Pawn(PawnId pawnId, TileId nestTileId) {
        this.playerId = pawnId.getPlayerId();
        this.pawnId = pawnId;
        this.nestTileId = new TileId(nestTileId);
        this.currentTileId = nestTileId;
    }

    public PawnId getPawnId() {
        return pawnId;
    }

    public void setPawnId(PawnId pawnId) {
        this.pawnId = pawnId;
    }

    public int getPlayerId() {
        //todo: replace playerId with playerUUID
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public TileId getCurrentTileId() {
        return currentTileId;
    }

    public void setCurrentTileId(TileId currentTileId) {
        if(currentTileId != null) {
            this.currentTileId = currentTileId;
        }
    }

    public TileId getNestTileId() {
        return nestTileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pawn pawn = (Pawn) o;
        return Objects.equals(pawnId, pawn.pawnId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pawnId);
    }

    @Override
    public String toString() {
        return "Pawn{" + pawnId +" - currently on "+ currentTileId +'}';
    }
}
