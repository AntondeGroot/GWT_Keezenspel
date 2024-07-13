package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static gwtks.GameStateUtil.createPawnAndPlaceOnBoard;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameStateOnKillTest {
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
    void KillPawnOnNormalTile_Forward(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(0,9));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, new TileId(0,10));

        // WHEN
        createMoveMessage(pawn1,1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(0,10), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMovePawn2().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,10), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnNormalTile_Backward(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(0,11));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, new TileId(0,10));

        // WHEN
        createMoveMessage(pawn1,-1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId(0,10));
        expectedMovement.add(pawn2.getNestTileId());

        // THEN response message is correct
        assertEquals(new TileId(0,10), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(expectedMovement, moveResponse.getMovePawn2());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,10), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnOtherStartTile_Forward(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(0,15));
        Pawn pawn2 = createPawnAndPlaceOnBoard(2, new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMovePawn2().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,0), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnOtherStartTile_Backward(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(1,1));
        Pawn pawn2 = createPawnAndPlaceOnBoard(2, new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,-1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMovePawn2().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,0), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnSection_Forward(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(0,9));
        Pawn pawn2 = createPawnAndPlaceOnBoard(2, new TileId(1,5));

        // WHEN
        createMoveMessage(pawn1,12);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,5), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMovePawn2().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,5), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnSection_Backward(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(1,5));
        Pawn pawn2 = createPawnAndPlaceOnBoard(2, new TileId(0,9));

        // WHEN
        createMoveMessage(pawn1,-12);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(0,9), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMovePawn2().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,9), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }

    private void createMoveMessage(Pawn pawn, int steps){
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setTileId(pawn.getCurrentTileId());
        moveMessage.setStepsPawn1(steps);
    }
}