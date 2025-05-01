package ADG.Games.Keezen.Move;

import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.TileId;
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
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

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
        /***
         * only display the non-null fields
         */
        StringBuilder sb = new StringBuilder("MoveResponse{\n");

        if (result != null) sb.append("    result = ").append(result).append(",\n");
        if (pawnId1 != null) sb.append("    pawnId 1 = ").append(pawnId1).append(",\n");
        if (pawnId2 != null) sb.append("    pawnId 2 = ").append(pawnId2).append(",\n");
        if (pawnIdKilled1 != null) sb.append("    pawnId Killed 1 = ").append(pawnIdKilled1).append(",\n");
        if (pawnIdKilled2 != null) sb.append("    pawnId Killed 2 = ").append(pawnIdKilled2).append(",\n");
        if (movePawn1 != null) sb.append("    movePawn 1 = ").append(movePawn1).append(",\n");
        if (movePawn2 != null) sb.append("    movePawn 2 = ").append(movePawn2).append(",\n");
        if (moveKilledPawn1 != null) sb.append("    moveKilledPawn 1 = ").append(moveKilledPawn1).append(",\n");
        if (moveKilledPawn2 != null) sb.append("    moveKilledPawn 2 = ").append(moveKilledPawn2).append(",\n");
        if (moveType != null) sb.append("    moveType = ").append(moveType).append(",\n");
        if (messageType != null) sb.append("    messageType = ").append(messageType).append(",\n");
        if (errorMessage != null) sb.append("    errorMessage = ").append(errorMessage).append(",\n");

        sb.append('}');
        return sb.toString();
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
