package ADG;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MoveType;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.CardEnum.JACK;
import static ADG.Games.Keezen.Move.MoveType.FORFEIT;
import static org.junit.jupiter.api.Assertions.*;


public class PawnAndCardSelectionJackTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnNest;
    private Pawn ownPawnOnFinish;
    private Pawn otherPawnOnBoard;
    private Pawn otherPawnOnNest;
    private Pawn otherPawnOnFinish;
    private Card jackCard = JACK.get();
    private Card nonJackCard;
    private PawnAndCardSelection pawnAndCardSelection;

    @BeforeEach
    void setup(){
        // pawns player playing
        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnNest = new Pawn(new PawnId("1", 2), new TileId("1", -1));
        ownPawnOnFinish = new Pawn(new PawnId("1", 3), new TileId("1", 16));
        // other player pawns
        otherPawnOnBoard = new Pawn(new PawnId("2", 1), new TileId("2", 0));
        otherPawnOnNest = new Pawn(new PawnId("2", 2), new TileId("2", -1));
        otherPawnOnFinish = new Pawn(new PawnId("2", 3), new TileId("2", 16));

        nonJackCard = new Card(0,5);
        pawnAndCardSelection = new PawnAndCardSelection();
        pawnAndCardSelection.disableUIForTests();

    }

    // TEST: OWN PAWN ON NEST
    @Test
    public void withJack_SelectNestPawn_PossibleToSelect(){
        /***
         * it is possible to select an invalid choice of your own pawn
         * you just can't move them
         */

        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertEquals(ownPawnOnNest, pawnAndCardSelection.getPawn1());
        assertEquals(jackCard, pawnAndCardSelection.getCard());
    }
    @Test
    public void test_SelectNestPawn_SetJack_PossibleToSelect(){
        /***
         * it is possible to select an invalid choice of your own pawn
         * you just can't move them
         */

        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // WHEN
        pawnAndCardSelection.setCard(jackCard);

        // THEN
        assertEquals(ownPawnOnNest, pawnAndCardSelection.getPawn1());
        assertEquals(jackCard, pawnAndCardSelection.getCard());
    }
    // other player on nest
    @Test
    public void withJack_SelectPawnOnBoard_AndOnNest_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }
    // other player on finish
    @Test
    public void test_SelectPawnOnBoard_SelectPawnOnFinish_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }

    // TEST: OWN PAWN ON BOARD
    @Test
    public void withJack_SelectPawnOnBoard_MoveTypeIsSwitch(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(MoveType.SWITCH, pawnAndCardSelection.getMoveType());
    }
    @Test
    public void selectPawnOnBoard_SetJack_MoveTypeIsSwitch(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.setCard(jackCard);

        // THEN
        assertEquals(MoveType.SWITCH, pawnAndCardSelection.getMoveType());
    }
    // TEST: FINISH
    @Test
    public void withJack_SelectPawnOnFinish_PossibleToSelect(){
        /***
         * it is possible to select an invalid choice of your own pawn
         * you just can't move them
         */

        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(ownPawnOnFinish, pawnAndCardSelection.getPawn1());
        assertEquals(jackCard, pawnAndCardSelection.getCard());
    }
    @Test
    public void test_SelectPawnOnFinish_SelectJack_PossibleToSelect(){
        /***
         * it is possible to select an invalid choice of your own pawn
         * you just can't move them
         */

        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // WHEN
        pawnAndCardSelection.setCard(jackCard);

        // THEN
        assertEquals(ownPawnOnFinish, pawnAndCardSelection.getPawn1());
        assertEquals(jackCard, pawnAndCardSelection.getCard());
    }
    // TEST: FORFEIT
    @Test
    public void withJack_Forfeit_ResetsCardAndPawn(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.setMoveType(FORFEIT);

        // THEN
        assertEquals(FORFEIT, pawnAndCardSelection.getMoveType());
        assertNull(pawnAndCardSelection.getCard());
        assertNull(pawnAndCardSelection.getPawnId1());
    }
    // TEST: DESELECT BY SELECTING TWICE
    @Test
    public void selectPawnTwice_Deselects(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId1());
    }

    @Test
    void withJack_ClickOnlyOnOpponent_Possible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");

        // WHEN
        pawnAndCardSelection.setCard(jackCard);
        pawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertNotEquals(otherPawnOnBoard.getPawnId(), pawnAndCardSelection.getPawnId1());
        assertEquals(otherPawnOnBoard.getPawnId(), pawnAndCardSelection.getPawnId2());
    }
    @Test
    void withoutJack_clickOnOtherPawn_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");

        // WHEN
        pawnAndCardSelection.setCard(nonJackCard);
        pawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId1());
        assertNull(pawnAndCardSelection.getPawnId2());
    }
    @Test
    void selectTwoPawnsWithJack_SetNormalCard_OnlyOwnPawnIsSelected(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(jackCard);
        pawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertEquals(otherPawnOnBoard.getPawnId(), pawnAndCardSelection.getPawnId2());

        // WHEN
        pawnAndCardSelection.setCard(nonJackCard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }
    @Test
    public void withJack_AddOtherPawnTwice_Deselects() {
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertEquals(otherPawnOnBoard, pawnAndCardSelection.getPawn2());

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnBoard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }

    @Test
    public void withoutJack_DeselectsOtherPawn() {
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(jackCard);
        pawnAndCardSelection.addPawn(otherPawnOnBoard);

        // WHEN
        pawnAndCardSelection.setCard(nonJackCard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }
    @Test
    public void withJack_SelectOtherPawnOnNest_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }
    @Test
    public void withJack_SelectOtherPawnOnFinish_NotPossible(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId2());
    }
    @Test
    public void withoutJack_ThenJack_ResetsStepsPawn1(){
        // GIVEN
        pawnAndCardSelection.setCard(new Card(0,5));

        // WHEN
        pawnAndCardSelection.setCard(jackCard);

        // THEN
        assertEquals(0, pawnAndCardSelection.getNrStepsPawn1());
    }
}
