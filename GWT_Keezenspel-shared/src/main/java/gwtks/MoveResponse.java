package gwtks;

import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class MoveResponse implements IsSerializable {
    private Integer nextPlayerId;
    private Integer pawnId1;
    private Integer pawnId2;
    //private Animate movement of pawn 1
    //private animate movement of pawn 2
    private MoveType moveType;

    public Integer getNextPlayerId() {
        return nextPlayerId;
    }

    public void setNextPlayerId(Integer nextPlayerId) {
        this.nextPlayerId = nextPlayerId;
    }

    public Integer getPawnId1() {
        return pawnId1;
    }

    public void setPawnId1(Integer pawnId1) {
        this.pawnId1 = pawnId1;
    }

    public Integer getPawnId2() {
        return pawnId2;
    }

    public void setPawnId2(Integer pawnId2) {
        this.pawnId2 = pawnId2;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }
}
