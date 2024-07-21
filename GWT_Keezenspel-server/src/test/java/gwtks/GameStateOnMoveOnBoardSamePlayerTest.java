package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.*;
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
    void twoPawnsOnePlayer_CannotEndOnTheSameTile(){
        // GIVEN
        Card card = givePlayerCard(0,1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(0,9));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,10));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,9), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(0,10), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOn15_PawnCannotBePlacedThere_Forwards(){
        // GIVEN
        Card card = givePlayerCard(0,1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(0,14));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,15));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,14), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(0,15), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOn15_PawnCannotBePlacedThere_Backwards(){
        // GIVEN
        Card card = givePlayerCard(0,-1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(1,0));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,15));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId(1,0), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(0,15), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOnOtherStart_PawnCannotBePlacedThere_Forwards(){
        // GIVEN
        Card card = givePlayerCard(0,1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(0,15));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(1,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId(0,15), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(1,0), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void PawnOnOtherStart_PawnCannotBePlacedThere_Backwards(){
        // GIVEN
        Card card = givePlayerCard(0,-1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,0), new TileId(1,1));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(1,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,1), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(new TileId(1,0), GameState.getPawn(pawn2).getCurrentTileId());
    }
}