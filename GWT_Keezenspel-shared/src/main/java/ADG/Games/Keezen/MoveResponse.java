package ADG.Games.Keezen;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.LinkedList;
import java.util.Objects;

@SuppressWarnings("serial")
public class MoveResponse implements IsSerializable {
    // serializable messages cannot contain List but must use a concrete implementation like ArrayList or LinkedList
    private PawnId pawnId1;
    private PawnId pawnId2;
    private PawnId pawnIdKilled1;
    private PawnId pawnIdKilled2;
    // deque is not supported by GWT
    private LinkedList<TileId> movePawn1;
    private LinkedList<TileId> movePawn2;
    private LinkedList<TileId> moveKilledPawn1;
    private LinkedList<TileId> moveKilledPawn2;
    private MoveType moveType;
    private MessageType messageType;
    private MoveResult result;

    public MoveResponse() {}

    public MoveResult getResult() {
        return result;
    }

    public void setResult(MoveResult result) {
        this.result = result;
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

    public PawnId getPawnIdKilled1() {
        return pawnIdKilled1;
    }

    public PawnId getPawnIdKilled2(){
        return pawnIdKilled2;
    }

    public void setPawnIdKilled1(PawnId pawnIdKilled) {
        this.pawnIdKilled1 = pawnIdKilled;
    }

    public void setPawnIdKilled2(PawnId pawnIdKilled) {
        this.pawnIdKilled2 = pawnIdKilled;
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

    public LinkedList<TileId> getMoveKilledPawn1() {
        return moveKilledPawn1;
    }

    public LinkedList<TileId> getMoveKilledPawn2() {
        return moveKilledPawn2;
    }

    /**
     * Only use this server side in OnMove to check if any pawn was killed
     * when you want to communicate to the client that 2 pawns were killed
     * then use SetMoveKilledPawn2 in OnSplit
     */
    public void setMoveKilledPawn1(LinkedList<TileId> moveKilledPawn) {
        this.moveKilledPawn1 = moveKilledPawn;
    }

    /**
     * Only use this when you use the OnSplit filling it in for the client
     */
    public void setMoveKilledPawn2(LinkedList<TileId> moveKilledPawn) {
        this.moveKilledPawn2 = moveKilledPawn;
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
        return "MoveResponse{\n" +
                "    result = " + result +",\n\n"+
                "    pawnId 1 = " + pawnId1 +",\n"+
                "    pawnId 2 = " + pawnId2 +",\n"+
                "    pawnId Killed 1 = " + pawnIdKilled1 +",\n"+
                "    pawnId Killed 2 = " + pawnIdKilled2 +",\n"+
                "    movePawn 1 = " + movePawn1 +",\n"+
                "    movePawn 2 = " + movePawn2 +",\n"+
                "    moveKilledPawn 1 = " + moveKilledPawn1 +",\n"+
                "    moveKilledPawn 2 = " + moveKilledPawn2 +",\n"+
                "    moveType = " + moveType +",\n"+
                "    messageType = " + messageType +",\n"+
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveResponse that = (MoveResponse) o;
        return Objects.equals(pawnId1, that.pawnId1) && Objects.equals(pawnId2, that.pawnId2) && Objects.equals(pawnIdKilled1, that.pawnIdKilled1) && Objects.equals(pawnIdKilled2, that.pawnIdKilled2) && Objects.equals(movePawn1, that.movePawn1) && Objects.equals(movePawn2, that.movePawn2) && Objects.equals(moveKilledPawn1, that.moveKilledPawn1) && Objects.equals(moveKilledPawn2, that.moveKilledPawn2) && moveType == that.moveType && messageType == that.messageType && result == that.result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pawnId1, pawnId2, pawnIdKilled1, pawnIdKilled2, movePawn1, movePawn2, moveKilledPawn1, moveKilledPawn2, moveType, messageType, result);
    }
}
