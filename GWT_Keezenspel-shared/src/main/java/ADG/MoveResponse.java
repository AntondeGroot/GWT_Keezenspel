package ADG;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.LinkedList;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class MoveResponse implements IsSerializable {
    // serializable messages cannot contain List but must use a concrete implementation like ArrayList or LinkedList
    private Integer nextPlayerId;
    private PawnId pawnId1;
    private PawnId pawnId2;
    // deque is not supported by GWT
    private LinkedList<TileId> movePawn1;
    private LinkedList<TileId> movePawn2;
    private MoveType moveType;
    private MessageType messageType;
    private MoveResult result;

    public MoveResult getResult() {
        return result;
    }

    public void setResult(MoveResult result) {
        this.result = result;
    }

    public Integer getNextPlayerId() {
        ArrayList<Integer> test = new ArrayList<>();
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

    public LinkedList<TileId> getMovePawn1() {
        return movePawn1;
    }

    public void setMovePawn1(LinkedList<TileId> movePawn1) {
        this.movePawn1 = movePawn1;
    }

    public LinkedList<TileId> getMovePawn2() {
        return movePawn2;
    }

    public void setMovePawn2(LinkedList<TileId> movePawn2) {
        this.movePawn2 = movePawn2;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "MoveResponse{" +
                "nextPlayerId=" + nextPlayerId +
                ", pawnId1=" + pawnId1 +
                ", pawnId2=" + pawnId2 +
                ", movePawn1=" + movePawn1 +
                ", movePawn2=" + movePawn2 +
                ", moveType=" + moveType +
                ", messageType=" + messageType +
                ", result=" + result +
                '}';
    }
}
