package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.createPawnAndPlaceOnBoard;
import static org.junit.jupiter.api.Assertions.*;

class GameStateOnMoveOnBoardTest {
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

    // moving
    @Test
    void passingStartForward_NotPossibleWhenPlayerIsThere_PawnMovesBackwards(){
        // when the pawn is on position 15 and takes two steps it will end up at position 13

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,15));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,2);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response is correct
        assertEquals(new TileId(0,13), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN GameState is correct
        assertEquals(new TileId(0,13), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void passingStartForward_NotPossibleWhenPlayerIsThere_PawnMovesForwardAndBack(){
        // when the pawn is on position 14 and takes 5 steps it will end up at position 11

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,14));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,5);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals( new TileId(0,11), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN GameState is correct
        assertEquals(new TileId(0,11), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void passingStartBackward_NotPossibleWhenPlayerIsThere_PawnMovesForward(){
        // when the pawn is on position 2 and takes -4 steps it will end up at position 4

        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(1,2));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,-4);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,4), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,4), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void passingStartBackward_PossibleWhenOtherPlayerIsThere_PawnMovesBackward(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,2));
        Pawn pawn2 = createPawnAndPlaceOnBoard(0, new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,-4);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(0,14), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,14), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void PawnMovesBackward_EndsOnStart(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,1));
        //Pawn pawn2 = createPawnAndPlaceOnBoard(0, new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,-1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,0), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,0), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void PawnMovesForward_EndsOnStart(){
        // GIVEN
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,15));
        //Pawn pawn2 = createPawnAndPlaceOnBoard(0, new TileId(1,0));

        // WHEN
        createMoveMessage(pawn1,1);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(2,0), moveResponse.getMovePawn1().get(0));  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(2,0), GameState.getPawn(pawn1).getCurrentTileId());
    }

    private void createMoveMessage(Pawn pawn, int steps){
        moveMessage.setPawnId1(pawn.getPawnId());
        moveMessage.setMoveType(MoveType.MOVE);
        moveMessage.setTileId(pawn.getCurrentTileId());
        moveMessage.setStepsPawn1(steps);
    }
}