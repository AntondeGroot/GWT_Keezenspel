package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.createPawnAndPlaceOnBoard;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameStateOnMoveOnFinishTilesTest {
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
    void moveOnFinishTileWhenAlmostThere(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(0,14));

        // WHEN
        createMoveMessage(pawn1,3);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,17), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTileWhenAlreadyOnAFinishTile(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,16));

        // WHEN
        createMoveMessage(pawn1,3);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,19), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,19), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveBackwardsOnFinishTileWhenAlreadyOnFinishTile(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,19));

        // WHEN
        createMoveMessage(pawn1,-2);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,17), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOutOfFinishTileWhenAlreadyOnFinishTile_ByMovingBackwards(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,19));

        // WHEN
        createMoveMessage(pawn1,-4);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(0,15), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,15), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOutOfFinishTileWhenAlreadyOnFinishTile_ByMovingForwards(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,19));

        // WHEN
        createMoveMessage(pawn1,4);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(0,15), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,15), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButRoomToMove_MoveBackwards(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,16));

        // WHEN
        createMoveMessage(pawn1,-5);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,18), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButRoomToMove_MoveForwards(){
        // pawn - x - x - pawn1
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,16));

        // WHEN
        createMoveMessage(pawn1,5);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,18), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButNoRoomToMove_MoveForwards(){
        // x - x - pawn - pawn1
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,18));

        // WHEN
        createMoveMessage(pawn1,5);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,19), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButNoRoomToMove_MoveBackwards(){
        // x - x - pawn - pawn1
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,18));

        // WHEN
        createMoveMessage(pawn1,-5);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,19), GameState.getPawn(pawn1).getCurrentTileId());
    }

    private void createMoveMessage(Pawn pawn, int steps){
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setTileId(pawn.getCurrentTileId());
        moveMessage.setStepsPawn1(steps);
    }
}