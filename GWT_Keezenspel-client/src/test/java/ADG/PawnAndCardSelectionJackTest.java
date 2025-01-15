package ADG;

import ADG.Games.Keezen.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.MoveType.FORFEIT;
import static org.junit.jupiter.api.Assertions.*;


public class PawnAndCardSelectionJackTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnNest;
    private Pawn ownPawnOnFinish;
    private Pawn otherPawnOnBoard;
    private Pawn otherPawnOnNest;
    private Pawn otherPawnOnFinish;
    private Card jackCard;
    private Card nonJackCard;

    @BeforeEach
    void setup(){
        PawnAndCardSelection.reset();

        // pawns player playing
        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnNest = new Pawn(new PawnId("1", 2), new TileId("1", -1));
        ownPawnOnFinish = new Pawn(new PawnId("1", 3), new TileId("1", 16));
        // other player pawns
        otherPawnOnBoard = new Pawn(new PawnId("2", 1), new TileId("2", 0));
        otherPawnOnNest = new Pawn(new PawnId("2", 2), new TileId("2", -1));
        otherPawnOnFinish = new Pawn(new PawnId("2", 3), new TileId("2", 16));

        jackCard = new Card(0,11);
        nonJackCard = new Card(0,5);
    }

    // TEST: OWN PAWN ON NEST
    @Test
    public void test_SetJack_SelectNestPawn_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
    }
    @Test
    public void test_SelectNestPawn_SetJack_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // WHEN
        PawnAndCardSelection.setCard(jackCard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
    }
    // other player on nest
    @Test
    public void test_SetJack_SelectPawnOnBoard_SelectPawnOnNest_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    // other player on finish
    @Test
    public void test_SelectPawnOnBoard_SelectPawnOnFinish_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    // TEST: OWN PAWN ON BOARD
    @Test
    public void test_SetJack_SelectPawnOnBoard_MoveOnSwitch(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(MoveType.SWITCH, PawnAndCardSelection.getMoveType());
    }
    @Test
    public void test_SelectPawnOnBoard_SetJack_MoveOnSwitch(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.setCard(jackCard);

        // THEN
        assertEquals(MoveType.SWITCH, PawnAndCardSelection.getMoveType());
    }
    // TEST: FINISH
    @Test
    public void test_SetJack_SelectPawnOnFinish_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
    }
    @Test
    public void test_SelectPawnOnFinish_SelectJack_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnFinish);

        // WHEN
        PawnAndCardSelection.setCard(jackCard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
    }
    // TEST: FORFEIT
    @Test
    public void test_SelectJack_ThenForfeit(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(jackCard);

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
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
    }

    @Test
    void test_SetJack_clickOnOtherPawn_Possible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");

        // WHEN
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertNotEquals(otherPawnOnBoard.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertEquals(otherPawnOnBoard.getPawnId(), PawnAndCardSelection.getPawnId2());
    }
    @Test
    void test_SetNonJack_clickOnOtherPawn_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");

        // WHEN
        PawnAndCardSelection.setCard(nonJackCard);
        PawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    @Test
    void test_selectTwoPawnsWithJack_OtherCardSelected_OnlyOwnPawnIsSelected(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertEquals(otherPawnOnBoard.getPawnId(), PawnAndCardSelection.getPawnId2());

        // WHEN
        PawnAndCardSelection.setCard(nonJackCard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    @Test
    public void test_SetJack_AddOtherPawn_AddOtherPawn_Deselects() {
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertEquals(otherPawnOnBoard, PawnAndCardSelection.getPawn2());

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void test_SetNonJack_DeselectsOtherPawn() {
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(otherPawnOnBoard);

        // WHEN
        PawnAndCardSelection.setCard(nonJackCard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    @Test
    public void test_SetJack_SelectOtherPawnOnNest_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    @Test
    public void test_SetJack_SelectOtherPawnOnFinish_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }
}
