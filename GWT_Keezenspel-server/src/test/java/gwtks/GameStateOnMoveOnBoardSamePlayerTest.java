package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.createPawnAndPlaceOnBoard;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameStateOnMoveOnBoardSamePlayerTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();

    @BeforeEach
    void setUp() {
        GameState gameState = new GameState(8);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
        moveMessage = null;
        moveResponse = null;

    }

    @Test
    void twoPawnsOnePlayer_CannotEndOnTheSameTile(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(0,9));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,10));

        // WHEN
        createMoveMessage(pawn1,1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,9), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(0,10), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOn15_PawnCannotBePlacedThere_Forwards(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(0,14));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,15));

        // WHEN
        createMoveMessage(pawn1,1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,14), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(0,15), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOn15_PawnCannotBePlacedThere_Backwards(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(1,0));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,15));

        // WHEN
        createMoveMessage(pawn1,-1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId(1,0), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(0,15), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOnOtherStart_PawnCannotBePlacedThere_Forwards(){
// GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(0,15));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId(0,15), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(1,0), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOnOtherStart_PawnCannotBePlacedThere_Backwards(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(1,1));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,-1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,1), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(1,0), GameState.getPawn(pawn2).getCurrentTileId());
    }

    private void createMoveMessage(Pawn pawn, int steps){
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setTileId(pawn.getCurrentTileId());
        moveMessage.setStepsPawn1(steps);
    }
}