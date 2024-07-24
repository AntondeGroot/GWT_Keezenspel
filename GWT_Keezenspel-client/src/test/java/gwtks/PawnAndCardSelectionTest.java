package gwtks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PawnAndCardSelectionTest {
    private Pawn pawn1;
    private Pawn pawn1OffBoard;
    private Pawn pawn2;
    private Pawn pawn2OffBoard;
    private Card aceCard;
    private Card jackCard;
    private Card nonJackCard;

    @BeforeEach
    void setup(){
        PawnAndCardSelection.reset();

        pawn1 = new Pawn(new PawnId(1, 1), new TileId(1, 0));
        pawn1OffBoard = new Pawn(new PawnId(1, 1), new TileId(1, -1));
        pawn2 = new Pawn(new PawnId(2, 1), new TileId(2, 0));
        pawn2OffBoard = new Pawn(new PawnId(2, 1), new TileId(2, -1));

        aceCard = new Card(0,0);
        jackCard = new Card(0,10);
        nonJackCard = new Card(0,5);
    }

    @Test
    void clickOnOwnPawn_IsPawn1() {
        // GIVEN
        PawnAndCardSelection.setPlayerId(1);

        // WHEN
        PawnAndCardSelection.addPawn(pawn1);

        // THEN
        assertEquals(pawn1.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertNotEquals(pawn1.getPawnId(), PawnAndCardSelection.getPawn2());
    }
    @Test
    void clickOnOtherPawn_CannotSelect() {
        // GIVEN
        PawnAndCardSelection.setPlayerId(1);

        // WHEN
        PawnAndCardSelection.addPawn(pawn2);

        // THEN
        assertNotEquals(pawn2.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    @Test
    void clickOnOtherPawn_CanSelectWhenJack(){
        // GIVEN
        PawnAndCardSelection.setPlayerId(1);

        // WHEN
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(pawn2);

        // THEN
        assertNotEquals(pawn2.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertEquals(pawn2.getPawnId(), PawnAndCardSelection.getPawnId2());
    }
    @Test
    void clickOnOtherPawn_CannotSelectWhenNotJack(){
        // GIVEN
        PawnAndCardSelection.setPlayerId(1);

        // WHEN
        PawnAndCardSelection.setCard(nonJackCard);
        PawnAndCardSelection.addPawn(pawn2);

        // THEN
        assertNotEquals(pawn2.getPawnId(), PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    @Test
    void selected2PawnsWithJack_WhenOtherCardSelected_OnlyOwnPawnIsSelected(){
        // GIVEN
        PawnAndCardSelection.setPlayerId(1);

        // WHEN
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(pawn2);
        // THEN
        assertEquals(pawn2.getPawnId(), PawnAndCardSelection.getPawnId2());

        // WHEN
        PawnAndCardSelection.setCard(new Card(0,0));
        // THEN
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testAddPawnSamePlayer() {
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);
        assertEquals(pawn1, PawnAndCardSelection.getPawn1());

        // Deselect the same pawn
        PawnAndCardSelection.addPawn(pawn1);
        assertNull(PawnAndCardSelection.getPawnId1());
    }

    @Test
    public void testAddPawnDifferentPlayerWithoutJack() {
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);

        PawnAndCardSelection.setPlayerId(2);
        PawnAndCardSelection.setCard(nonJackCard);
        PawnAndCardSelection.addPawn(pawn2);

        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testAddPawnDifferentPlayerWithJack_SelectAndDeselect() {
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(pawn2);

        assertEquals(pawn2, PawnAndCardSelection.getPawn2());

        // Deselect the same pawn
        PawnAndCardSelection.addPawn(pawn2);
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
    public void testSetCardNonJackDeselectsPawn() {
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(pawn2);

        assertEquals(pawn2, PawnAndCardSelection.getPawn2());

        PawnAndCardSelection.setCard(nonJackCard);
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void testSelectAndDeselectPawn_SelectCard_PawnStillDeselected() {
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.setCard(jackCard);

        assertNull(PawnAndCardSelection.getPawnId1());
    }

    @Test
    public void testChangingPlayerIdDeselectsPawnsAndCards() {
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.setCard(jackCard);
        PawnAndCardSelection.addPawn(pawn2);

        PawnAndCardSelection.setPlayerId(2);

        assertNull(PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
        assertNull(PawnAndCardSelection.getCard());
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
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.setCard(jackCard);

        PawnAndCardSelection.reset();

        assertNull(PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
        assertNull(PawnAndCardSelection.getCard());
        assertTrue(PawnAndCardSelection.getDrawCards());
    }

    @Test
    public void testSetAceThenSelectPawnOffBoard_MoveOnBoard(){
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.setCard(aceCard);
        PawnAndCardSelection.addPawn(pawn1OffBoard);

        assertEquals(MoveType.ONBOARD, PawnAndCardSelection.getMoveType());
    }
    @Test
    public void testSelectPawnOffBoardThenSelectAce_MoveOnBoard(){
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1OffBoard);
        PawnAndCardSelection.setCard(aceCard);

        assertEquals(MoveType.ONBOARD, PawnAndCardSelection.getMoveType());
    }
    @Test
    public void testSetAceThenSelectPawnOnBoard_MoveOnMove(){
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.setCard(aceCard);
        PawnAndCardSelection.addPawn(pawn1);

        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }
    @Test
    public void testSelectPawnOnBoardThenSelectAce_MoveOnMove(){
        PawnAndCardSelection.setPlayerId(1);
        PawnAndCardSelection.addPawn(pawn1);
        PawnAndCardSelection.setCard(aceCard);

        assertEquals(MoveType.MOVE, PawnAndCardSelection.getMoveType());
    }

    private Pawn createPawn(int playerId){
        return new Pawn(new PawnId(playerId, 1), new TileId(playerId, 0));
    }
}