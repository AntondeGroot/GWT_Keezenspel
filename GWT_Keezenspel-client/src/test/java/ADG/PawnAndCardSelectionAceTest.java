package ADG;

import ADG.Games.Keezen.*;
import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MoveType;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.CardEnum.ACE;
import static ADG.Games.Keezen.Move.MoveType.FORFEIT;
import static ADG.Games.Keezen.Move.MoveType.MOVE;
import static ADG.Games.Keezen.Move.MoveType.ONBOARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PawnAndCardSelectionAceTest {
    private Pawn ownPawnOnBoard;
    private Pawn ownPawnOnNest;
    private Pawn ownPawnOnFinish;
    private PawnAndCardSelection pawnAndCardSelection;

    @BeforeEach
    void setup(){
        pawnAndCardSelection = new PawnAndCardSelection();
        pawnAndCardSelection.disableUIForTests();

        ownPawnOnBoard = new Pawn(new PawnId("1", 1), new TileId("1", 0));
        ownPawnOnNest = new Pawn(new PawnId("1", 2), new TileId("1", -1));
        ownPawnOnFinish = new Pawn(new PawnId("1", 3), new TileId("1", 16));

    }

    // TEST: NEST
    @Test
    public void setAce_SelectPawnOffBoard_MoveOnBoard(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(ACE.get());

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // THEN
        assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
    }
    @Test
    public void selectPawnOffBoard_SetAce_MoveOnBoard(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnNest);

        // WHEN
        pawnAndCardSelection.setCard(ACE.get());

        // THEN
        assertEquals(MoveType.ONBOARD, pawnAndCardSelection.getMoveType());
    }
    // TEST: ON BOARD
    @Test
    public void setAce_SelectPawnOnBoard_MoveTypeMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(ACE.get());

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertEquals(MOVE, pawnAndCardSelection.getMoveType());
    }
    @Test
    public void selectPawnOnBoard_SelectAce_MoveTypeMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // WHEN
        pawnAndCardSelection.setCard(ACE.get());

        // THEN
        assertEquals(MOVE, pawnAndCardSelection.getMoveType());
    }
    // TEST: FINISH
    @Test
    public void setAce_SelectPawnOnFinish_MoveTypeMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(ACE.get());

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // THEN
        assertEquals(MOVE, pawnAndCardSelection.getMoveType());
    }
    @Test
    public void selectPawnOnFinish_SelectAce_MoveTypeMove(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnFinish);

        // WHEN
        pawnAndCardSelection.setCard(ACE.get());

        // THEN
        assertEquals(MOVE, pawnAndCardSelection.getMoveType());
    }
    // TEST: FORFEIT
    @Test
    public void selectAce_ThenForfeit_Resets(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(ACE.get());

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
        pawnAndCardSelection.setCard(ACE.get());

        // WHEN
        pawnAndCardSelection.addPawn(ownPawnOnBoard);

        // THEN
        assertNull(pawnAndCardSelection.getPawnId1());
    }
    @Test
    public void ONBOARD_SetAce_NonAce_ThenAce_MOVEStillONBOARD(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.setCard(ACE.get());
        pawnAndCardSelection.addPawn(ownPawnOnNest);
        pawnAndCardSelection.setCard(new Card(0,5));
        // giving a non ace card deselects the pawn, but when you select an Ace and the pawn
        // you should no longer see nrStepsPawn1 as 5.

        // WHEN
        pawnAndCardSelection.setCard(ACE.get());

        // THEN
        assertEquals(ONBOARD, pawnAndCardSelection.getMoveType());
        assertEquals(0, pawnAndCardSelection.getNrStepsPawn1());
    }
    @Test
    public void pawnOnBoard_SelectAce_Move(){
        // GIVEN
        pawnAndCardSelection.setPlayerId("1");
        pawnAndCardSelection.addPawn(ownPawnOnBoard);
        pawnAndCardSelection.setCard(new Card(0,5));
        pawnAndCardSelection.setCard(ACE.get());


        // THEN
        assertEquals(MOVE, pawnAndCardSelection.getMoveType());
        assertEquals(1, pawnAndCardSelection.getNrStepsPawn1());
    }
}
