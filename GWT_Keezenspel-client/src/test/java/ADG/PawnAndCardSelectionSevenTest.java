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
    public void withSeven_Select2Pawns_MoveTypeSplit(){
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
    public void withSeven_PawnOnNest_NotPossible(){
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
    public void withSeven_SelectPawn_MoveTypeMove(){
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
    public void withSeven_PawnOnFinish_MoveTypeMove(){
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
    public void withSeven_PawnOnBoardAndFinish_Possible(){
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
    public void withSeven_SelectTwoOwnPawnsOnBoard(){
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
    public void withSeven_CanOnlySelectOwnPawn(){
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
    public void withSeven_SelectTwoPawns_SetOtherCard_DeselectPawn2(){
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
    public void withSeven_CannotSelectSecondPawnOnNest(){
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
    public void withSeven_SelectPawn_DeselectSecondPawn_MoveTypeMove(){
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
    public void withSeven_SelectPawn_nrStepsIs7(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(sevenCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(7, pawnAndCardSelection.getNrStepsPawn1());
    }

    @Test
    public void withSeven_Select2Pawns_DeselectPawn1_Pawn2IsNowPawn1(){
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
    public void withSeven_Select2Pawns_SetJack_DeselectsPawn2_bugfix(){
        // this bug occurred because both Seven and Jack allow two pawns to be selected
        // and you can't switch two of your own pawns with a Jack

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
