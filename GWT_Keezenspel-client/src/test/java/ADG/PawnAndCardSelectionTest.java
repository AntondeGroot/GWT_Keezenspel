package ADG;

import ADG.Games.Keezen.*;
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

    @BeforeEach
    void setup(){
        PawnAndCardSelection.reset();

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
        PawnAndCardSelection.setPlayerId("1");

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(ownPawnOnBoard.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertNotEquals(ownPawnOnBoard.getPawnId(), PawnAndCardSelection.getPawn2());
    }
    @Test
    void clickOnOtherPawn_CannotSelect() {
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");

        // WHEN
        PawnAndCardSelection.addPawn(otherPawn);

        // THEN
        assertNotEquals(otherPawn.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testAddPawnSamePlayer() {
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        assertEquals(ownPawnOnBoard, PawnAndCardSelection.getPawn1());

        // Deselect the same pawn
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        assertNull(PawnAndCardSelection.getPawnId1());
    }

    @Test
    public void testAddPawnDifferentPlayerWithoutJack() {
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);

        PawnAndCardSelection.setPlayerId("2");
        PawnAndCardSelection.setCard(nonJackCard);
        PawnAndCardSelection.addPawn(otherPawn);

        assertNull(PawnAndCardSelection.getPawnId2());
    }



    @Test
    public void testSetAndGetCard() {
        PawnAndCardSelection.setCard(jackCard);
        assertEquals(jackCard, PawnAndCardSelection.getCard());

        PawnAndCardSelection.setCard(nonJackCard);
        assertEquals(nonJackCard, PawnAndCardSelection.getCard());
    }

    @Test
    public void testDrawCardsFlag() {
        PawnAndCardSelection.setCardsAreDrawn();
        assertFalse(PawnAndCardSelection.getDrawCards());

        PawnAndCardSelection.setCard(jackCard);
        assertTrue(PawnAndCardSelection.getDrawCards());
    }

    @Test
    public void testReset() {
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnBoard);
        PawnAndCardSelection.setCard(jackCard);

        PawnAndCardSelection.reset();

        assertNull(PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
        assertNull(PawnAndCardSelection.getCard());
        assertTrue(PawnAndCardSelection.getDrawCards());
    }

    @Test
    public void testSetJack_SelectPawn1And2_CannotSelectPawnOffBoard(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertEquals(ownPawnOnBoard2.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testSetJack_SelectPawn1And2_CannotSelectPawnOnFinish(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(ownPawnOnBoard2);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertEquals(ownPawnOnBoard2.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }
}