package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.*;
import static gwtks.MoveResult.CANNOT_MAKE_MOVE;
import static gwtks.MoveResult.CAN_MAKE_MOVE;
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
        CardsDeck.setNrPlayers(8);
        CardsDeck.shuffle();
        CardsDeck.dealCards();
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
        moveMessage = null;
        moveResponse = null;
        CardsDeck.reset();
    }

    @Test
    void moveFromLastSectionOntoFinish(){
        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(0,14));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId(1,17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveFurtherDownFinishTile(){
        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,16));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId(1,19), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,19), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveBackwardsOnFinishTiles_NegativeSteps(){
        // GIVEN
        Card card = givePlayerCard(1,-2);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,19));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveBackwardsOnFinishTiles_NegativeSteps_NotEndposition(){
        // GIVEN
        Card card = givePlayerCard(1,-1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveBackwardsOnFinishTiles_PositiveSteps(){
        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId(1,17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,17), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void MoveOutOfFinishTileWhenAlreadyOnFinishTile_ByMovingBackwards(){
        // GIVEN
        Card card = givePlayerCard(1,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,19));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId(0,15), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,15), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOutOfFinishTileWhenAlreadyOnFinishTile_ByMovingForwards(){
        // GIVEN
        Card card = givePlayerCard(1,4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,19));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(0,15), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,15), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButRoomToMove_MoveBackwards(){
        // GIVEN
        Card card = givePlayerCard(1,-5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,16));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,18), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButRoomToMove_MoveForwards(){
        // pawn - x - x - pawn1
        // GIVEN
        Card card = givePlayerCard(1,5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,16));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,18), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void whenPawnClosedInAtTile19_PositiveSteps_DontMove(){
        // x - x - pawn - pawn1
        // GIVEN
        Card card = givePlayerCard(1,5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId(1,19), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void whenPawnClosedInAtTile18_DontMove(){
        // x - x - pawn - pawn1
        // GIVEN
        Card card = givePlayerCard(1,5);
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,18));
        Pawn pawn3 = createPawnAndPlaceOnBoard(new PawnId(1,2), new TileId(1,17));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId(1,18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void whenPawnClosedInAtTile19_NegativeSteps_DontMove(){
        // x - x - pawn - pawn1
        // GIVEN
        Card card = givePlayerCard(1,-5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,0), new TileId(1,19));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(1,18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);
        System.out.println(CardsDeck.getCardsForPlayer(1).toString());

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,19), GameState.getPawn(pawn1).getCurrentTileId());
    }
}