package ADG;

import ADG.Games.Keezen.*;
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

    @BeforeEach
    void setup(){
        PawnAndCardSelection.reset();

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
    public void test_SetKing_SelectPawnOffBoard_MoveOnBoard(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(kingCard);

        // WHEN
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertEquals(MoveType.ONBOARD, PawnAndCardSelection.getMoveType());
    }

    // TEST: NEST
    @Test
    public void test_SelectPawnOffBoard_SetKing_MoveOnBoard(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnNest);

        // WHEN
        PawnAndCardSelection.setCard(kingCard);

        // THEN
        assertEquals(MoveType.ONBOARD, PawnAndCardSelection.getMoveType());
    }

    @Test
    public void test_SetKing_SelectOtherPawnOnNest_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(kingCard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnNest);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }

    @Test
    public void test_SetKing_SelectOtherPawnOnFinish_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.setCard(kingCard);

        // WHEN
        PawnAndCardSelection.addPawn(otherPawnOnFinish);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());
        assertNull(PawnAndCardSelection.getPawnId2());
    }
    @Test
    public void test_SelectOwnPawnOnFinish_SetKing_NotPossible(){
        // GIVEN
        PawnAndCardSelection.setPlayerId("1");
        PawnAndCardSelection.addPawn(ownPawnOnFinish);

        // WHEN
        PawnAndCardSelection.setCard(kingCard);

        // THEN
        assertNull(PawnAndCardSelection.getPawnId1());// todo: or should the card be empty?
    }
}
