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

public class PawnAndCardSelectionKingTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnNest;
    private Pawn ownPawnOnFinish;
    private Pawn otherPawnOnNest;
    private Pawn otherPawnOnFinish;
    private Card kingCard;
    private PawnAndCardSelection pawnAndCardSelection;

    @BeforeEach
    void setup(){
        pawnAndCardSelection = new PawnAndCardSelection();
        pawnAndCardSelection.disableUIForTests();

        // pawns player playing
        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnNest = new Pawn(new PawnId("1", 2), new TileId("1", -1));
        ownPawnOnFinish = new Pawn(new PawnId("1", 3), new TileId("1", 16));
        // other player pawns
        otherPawnOnNest = new Pawn(new PawnId("2", 1), new TileId("2", -1));
        otherPawnOnFinish = new Pawn(new PawnId("2", 2), new TileId("2", 16));

        kingCard = new Card(0,13);
    }

    // TEST: NEST
    @Test
    public void withKing_SelectPawnOffBoard_MoveTypeOBoard(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(kingCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
    }

    // TEST: NEST
    @Test
    public void selectPawnOffBoard_SetKing_MoveTypeOnBoard(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // WHEN
        pawnAndCardSelection.setCard(kingCard);

        // THEN
        assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
    }

    @Test
    public void withKing_SelectOtherPawnOnNest_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(kingCard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId1());
        assertNull(pawnAndCardSelection.getPawnId2());
    }

    @Test
    public void withKing_SelectOtherPawnOnFinish_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(kingCard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId1());
        assertNull(pawnAndCardSelection.getPawnId2());
    }
    @Test
    public void selectOwnPawnOnFinish_SetKing_Possible(){
        /***
         * The test move will show if you can make the move
         * We do not want to client to deselect the pawn based on a wrongly selected card
         */
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // WHEN
        pawnAndCardSelection.setCard(kingCard);

        // THEN
        assertEquals(ownPawnOnFinish.getPawnId(), pawnAndCardSelection.getPawnId1());
    }

    @Test
    public void withNormalCard_ThenKing_ResetsStepsPawn1(){
        // GIVEN
        pawnAndCardSelection.setCard(new Card(0,5));

        // WHEN
        pawnAndCardSelection.setCard(kingCard);

        // THEN
        assertEquals(0, pawnAndCardSelection.getNrStepsPawn1());
    }
}
