package ADG;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MoveType;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.Move.MoveType.FORFEIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PawnAndCardSelectionAceTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnNest;
    private Pawn ownPawnOnFinish;
    private Card aceCard;
    private PawnAndCardSelection pawnAndCardSelection;

    @BeforeEach
    void setup(){
        pawnAndCardSelection = new PawnAndCardSelection();

        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnNest = new Pawn(new PawnId("1", 2), new TileId("1", -1));
        ownPawnOnFinish = new Pawn(new PawnId("1", 3), new TileId("1", 16));

        aceCard = new Card(0,1);
    }

    // TEST: NEST
    @Test
    public void test_SetAce_SelectPawnOffBoard_MoveOnBoard(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(aceCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
    }
    @Test
    public void test_SelectPawnOffBoard_SetAce_MoveOnBoard(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // WHEN
        pawnAndCardSelection.setCard(aceCard);

        // THEN
        assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
    }
    // TEST: ON BOARD
    @Test
    public void test_SetAce_SelectPawnOnBoard_MoveOnMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(aceCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
    }
    @Test
    public void test_SelectPawnOnBoard_SelectAce_MoveOnMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.setCard(aceCard);

        // THEN
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
    }
    // TEST: FINISH
    @Test
    public void test_SetAce_SelectPawnOnFinish_MoveOnMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(aceCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
    }
    @Test
    public void test_SelectPawnOnFinish_SelectAce_MoveOnMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // WHEN
        pawnAndCardSelection.setCard(aceCard);

        // THEN
        assertEquals(MoveType.MOVE, pawnAndCardSelection.getMoveType());
    }
    // TEST: FORFEIT
    @Test
    public void test_SelectAce_ThenForfeit(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(aceCard);

        // WHEN
        pawnAndCardSelection.setMoveType(FORFEIT);

        // THEN
        assertEquals(FORFEIT, pawnAndCardSelection.getMoveType());
        assertNull(pawnAndCardSelection.getCard());
        assertNull(pawnAndCardSelection.getPawnId1());
    }
    // TEST: DESELECT BY SELECTING TWICE
    @Test
    public void test_SelectPawnTwice_Deselects(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(aceCard);

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId1());
    }
}
