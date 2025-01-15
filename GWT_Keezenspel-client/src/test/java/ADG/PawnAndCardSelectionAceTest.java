package ADG;

import ADG.Games.Keezen.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.MoveType.FORFEIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PawnAndCardSelectionAceTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnNest;
    private Pawn ownPawnOnFinish;
    private Card aceCard;

    @BeforeEach
    void setup(){
        PawnAndCardSelection.reset();

        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnNest = new Pawn(new PawnId("1", 2), new TileId("1", -1));
        ownPawnOnFinish = new Pawn(new PawnId("1", 3), new TileId("1", 16));

        aceCard = new Card(0,1);
    }

    // TEST: NEST
    @Test
    public void test_SetAce_SelectPawnOffBoard_MoveOnBoard(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(aceCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertEquals(MoveType.ONBOARD, PawnAndCardSelection.getMoveType());
    }
    @Test
    public void test_SelectPawnOffBoard_SetAce_MoveOnBoard(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // WHEN
        PawnAndCardSelection.setCard(aceCard);

        // THEN
        assertEquals(MoveType.ONBOARD, PawnAndCardSelection.getMoveType());
    }
    // TEST: ON BOARD
    @Test
    public void test_SetAce_SelectPawnOnBoard_MoveOnMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(aceCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }
    @Test
    public void test_SelectPawnOnBoard_SelectAce_MoveOnMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.setCard(aceCard);

        // THEN
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }
    // TEST: FINISH
    @Test
    public void test_SetAce_SelectPawnOnFinish_MoveOnMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(aceCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }
    @Test
    public void test_SelectPawnOnFinish_SelectAce_MoveOnMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnFinish);

        // WHEN
        PawnAndCardSelection.setCard(aceCard);

        // THEN
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }
    // TEST: FORFEIT
    @Test
    public void test_SelectAce_ThenForfeit(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(aceCard);

        // WHEN
        PawnAndCardSelection.setMoveType(FORFEIT);

        // THEN
        assertEquals(FORFEIT, PawnAndCardSelection.getMoveType());
        assertNull(PawnAndCardSelection.getCard());
        assertNull(PawnAndCardSelection.getPawnId1());
    }
    // TEST: DESELECT BY SELECTING TWICE
    @Test
    public void test_SelectPawnTwice_Deselects(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(aceCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
    }
}
