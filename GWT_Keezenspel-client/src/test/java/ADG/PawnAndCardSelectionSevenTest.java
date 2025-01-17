package ADG;

import ADG.Games.Keezen.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PawnAndCardSelectionSevenTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnBoard2;
    private Pawn ownPawnOnNest;
    private Pawn ownPawnOnFinish;
    private Pawn otherPawnOnBoard;
    private Card sevenCard;
    private Card nonJackCard;
    private Card jackCard;

    @BeforeEach
    void setup(){
        PawnAndCardSelection.reset();

        // pawns player playing
        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnBoard2 = new Pawn(new PawnId("1", 2), new TileId("1", 0));
        ownPawnOnNest = new Pawn(new PawnId("1", 3), new TileId("1", -1));
        ownPawnOnFinish = new Pawn(new PawnId("1", 4), new TileId("1", 16));
        // other player pawns
        otherPawnOnBoard = new Pawn(new PawnId("2", 1), new TileId("2", 0));

        sevenCard = new Card(0,7);
        nonJackCard = new Card(0,5);
        jackCard = new Card(0,11);
    }

    @Test
    public void test_SetSeven_SelectPawn1_SelectPawn2_MoveTypeSplit(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertEquals(MoveType.SPLIT, PawnAndCardSelection.getMoveType());
    }

    // TEST nest
    @Test
    public void test_SetSeven_PawnOnNest_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
    }
    // test board
    @Test
    public void test_SetSeven_SelectPawn_MoveTypeMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }
    // test finish
    @Test
    public void test_SetSeven_PawnOnFinish_MoveOnMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
        assertEquals(ownPawnOnFinish, PawnAndCardSelection.getPawn1());
    }

    // test board and finish
    @Test
    public void test_SetSeven_PawnOnBoard_PawnOnFinish_Possible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(MoveType.SPLIT, PawnAndCardSelection.getMoveType());
        assertEquals(ownPawnOnBoard, PawnAndCardSelection.getPawn1());
        assertEquals(ownPawnOnFinish, PawnAndCardSelection.getPawn2());
    }
    // test board and board
    @Test
    public void test_SetSeven_SelectPawn1_SelectPawn2_Pawn2IsSelected(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertEquals(ownPawnOnBoard, PawnAndCardSelection.getPawn1());
        assertEquals(ownPawnOnBoard2, PawnAndCardSelection.getPawn2());
    }
    // test board and board2
    @Test
    public void test_SetSeven_SelectOwnPawn_SelectOtherPawn_MoveTypeMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void test_SetSeven_SelectPawn1And2_SetOtherCard_DeselectPawn2(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        PawnAndCardSelection.setCard(nonJackCard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testSetSeven_SelectPawn_CannotSelectPawnOnNest(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }

    @Test
    public void testSetSeven_SelectPawn_DeselectSecondPawn_MoveOnMove(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertEquals(MoveType.SPLIT, PawnAndCardSelection.getMoveType());

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }

    // test nr steps is correct
    @Test
    public void test_SetSeven_Select1Pawn_nrStepsIs7(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(7, PawnAndCardSelection.getNrStepsPawn1());
    }

    @Test
    public void test_SetSeven_Select2Pawns_DeselectPawn1_Pawn2IsNowPawn1(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(7, PawnAndCardSelection.getNrStepsPawn1());
        assertNull(PawnAndCardSelection.getPawnId2());
        assertEquals(ownPawnOnBoard2, PawnAndCardSelection.getPawn1());
    }

    @Test
    public void test_setSeven_Select2Pawns_SetJack_DeselectsPawn2_bugfix(){
        // this bug occurred because both Seven and Jack allow two pawns to be selected

        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(sevenCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        PawnAndCardSelection.setCard(jackCard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }
}
