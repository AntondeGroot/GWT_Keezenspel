package ADG;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PawnAndCardSelectionTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnBoard2;
    private Pawn otherPawn;
    private Pawn otherPawnOnNest;
    private Pawn otherPawnOnFinish;
    private Card jackCard;
    private Card nonJackCard;
    private PawnAndCardSelection pawnAndCardSelection;

    @BeforeEach
    void setup(){
        pawnAndCardSelection = new PawnAndCardSelection();
        pawnAndCardSelection.disableUIForTests();

        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnBoard2 = new Pawn(new PawnId("1", 2), new TileId("1", 5));
        otherPawn = new Pawn(new PawnId("2", 1), new TileId("2", 0));
        otherPawnOnNest = new Pawn(new PawnId("2", 1), new TileId("2", -1));
        otherPawnOnFinish = new Pawn(new PawnId("2", 1), new TileId("2", 16));

        jackCard = new Card(0,11);
        nonJackCard = new Card(0,5);
    }

    @Test
    void clickOnOwnPawn_IsPawn1() {
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(ownPawnOnBoard.getPawnId(), pawnAndCardSelection.getPawnId1());
        assertNotEquals(ownPawnOnBoard.getPawnId(), pawnAndCardSelection.getPawn2());
    }
    @Test
    void clickOnOtherPawn_CannotSelect() {
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");

        // WHEN
        pawnAndCardSelection.addPawn(otherPawn);

        // THEN
        assertNotEquals(otherPawn.getPawnId(), pawnAndCardSelection.getPawnId1());
        assertNull(pawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testAddPawnSamePlayer() {
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        assertEquals(ownPawnOnBoard, pawnAndCardSelection.getPawn1());

        // Deselect the same pawn
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        assertNull(pawnAndCardSelection.getPawnId1());
    }

    @Test
    public void testAddPawnDifferentPlayerWithoutJack() {
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        pawnAndCardSelection.setPlayerId("2");
        pawnAndCardSelection.setCard(nonJackCard);
        pawnAndCardSelection.addPawn(otherPawn);

        assertNull(pawnAndCardSelection.getPawnId2());
    }



    @Test
    public void testSetAndGetCard() {
        pawnAndCardSelection.setCard(jackCard);
        assertEquals(jackCard, pawnAndCardSelection.getCard());

        pawnAndCardSelection.setCard(nonJackCard);
        assertEquals(nonJackCard, pawnAndCardSelection.getCard());
    }

    @Test
    public void testDrawCardsFlag() {
        pawnAndCardSelection.setCardsAreDrawn();
        assertFalse(pawnAndCardSelection.getDrawCards());

        pawnAndCardSelection.setCard(jackCard);
        assertTrue(pawnAndCardSelection.getDrawCards());
    }

    @Test
    public void testReset() {
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(jackCard);

        pawnAndCardSelection.reset();

        assertNull(pawnAndCardSelection.getPawnId1());
        assertNull(pawnAndCardSelection.getPawnId2());
        assertNull(pawnAndCardSelection.getCard());
        assertTrue(pawnAndCardSelection.getDrawCards());
    }

    @Test
    public void withJack_CannotSelectOtherPawnOnNest(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertEquals(ownPawnOnBoard2.getPawnId(), pawnAndCardSelection.getPawnId1());
        assertNull(pawnAndCardSelection.getPawnId2());
    }

    @Test
    public void withJack_CannotSelectOtherPawnOnFinish(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(jackCard);
        pawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        pawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertEquals(ownPawnOnBoard2.getPawnId(), pawnAndCardSelection.getPawnId1());
        assertNull(pawnAndCardSelection.getPawnId2());
    }
}