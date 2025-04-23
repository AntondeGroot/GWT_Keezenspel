package ADG;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MoveType;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
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
    private PawnAndCardSelection pawnAndCardSelection;

    @BeforeEach
    void setup(){
        pawnAndCardSelection = new PawnAndCardSelection();
        pawnAndCardSelection.disableUIForTests();

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
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertEquals(MoveType.SPLIT, pawnAndCardSelection.getMoveType());
    }

    // TEST nest
    @Test
    public void test_SetSeven_PawnOnNest_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId1());
    }
    // test board
    @Test
    public void test_SetSeven_SelectPawn_MoveTypeMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
    }
    // test finish
    @Test
    public void test_SetSeven_PawnOnFinish_MoveOnMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
        assertEquals(ownPawnOnFinish, pawnAndCardSelection.getPawn1());
    }

    // test board and finish
    @Test
    public void test_SetSeven_PawnOnBoard_PawnOnFinish_Possible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(MoveType.SPLIT, pawnAndCardSelection.getMoveType());
        assertEquals(ownPawnOnBoard, pawnAndCardSelection.getPawn1());
        assertEquals(ownPawnOnFinish, pawnAndCardSelection.getPawn2());
    }
    // test board and board
    @Test
    public void test_SetSeven_SelectPawn1_SelectPawn2_Pawn2IsSelected(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertEquals(ownPawnOnBoard, pawnAndCardSelection.getPawn1());
        assertEquals(ownPawnOnBoard2, pawnAndCardSelection.getPawn2());
    }
    // test board and board2
    @Test
    public void test_SetSeven_SelectOwnPawn_SelectOtherPawn_MoveTypeMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
        assertNull(pawnAndCardSelection.getPawnId2());
    }

    @Test
    public void test_SetSeven_SelectPawn1And2_SetOtherCard_DeselectPawn2(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        pawnAndCardSelection.setCard(nonJackCard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testSetSeven_SelectPawn_CannotSelectPawnOnNest(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
    }

    @Test
    public void testSetSeven_SelectPawn_DeselectSecondPawn_MoveOnMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertEquals(MoveType.SPLIT, pawnAndCardSelection.getMoveType());

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
    }

    // test nr steps is correct
    @Test
    public void test_SetSeven_Select1Pawn_nrStepsIs7(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(7, pawnAndCardSelection.getNrStepsPawn1());
    }

    @Test
    public void test_SetSeven_Select2Pawns_DeselectPawn1_Pawn2IsNowPawn1(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(7, pawnAndCardSelection.getNrStepsPawn1());
        assertNull(pawnAndCardSelection.getPawnId2());
        assertEquals(ownPawnOnBoard2, pawnAndCardSelection.getPawn1());
    }

    @Test
    public void test_setSeven_Select2Pawns_SetJack_DeselectsPawn2_bugfix(){
        // this bug occurred because both Seven and Jack allow two pawns to be selected

        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        pawnAndCardSelection.setCard(jackCard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }
}
