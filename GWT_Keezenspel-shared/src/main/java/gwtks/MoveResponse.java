package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

@SuppressWarnings("serial")
public class MoveResponse implements IsSerializable {
    private Integer nextPlayerId;
    private PawnId pawnId1;
    private PawnId pawnId2;
    private List<TileId> movePawn1;
    private List<TileId> movePawn2;

    public Integer getNextPlayerId() {
        return nextPlayerId;
    }

    public void setNextPlayerId(Integer nextPlayerId) {
        this.nextPlayerId = nextPlayerId;
    }

    public PawnId getPawnId1() {
        return pawnId1;
    }

    public void setPawnId1(PawnId pawnId1) {
        this.pawnId1 = pawnId1;
    }

    public PawnId getPawnId2() {
        return pawnId2;
    }

    public void setPawnId2(PawnId pawnId2) {
        this.pawnId2 = pawnId2;
    }

    public List<TileId> getMovePawn1() {
        return movePawn1;
    }

    public void setMovePawn1(List<TileId> movePawn1) {
        this.movePawn1 = movePawn1;
    }

    public List<TileId> getMovePawn2() {
        return movePawn2;
    }

    public void setMovePawn2(List<TileId> movePawn2) {
        this.movePawn2 = movePawn2;
    }

    @Override
    public String toString() {
        return "MoveResponse{" +
                "nextPlayerId=" + nextPlayerId +
                ", pawnId1=" + pawnId1 +
                ", pawnId2=" + pawnId2 +
                ", movePawn1=" + movePawn1 +
                ", movePawn2=" + movePawn2 +
                '}';
    }
}
