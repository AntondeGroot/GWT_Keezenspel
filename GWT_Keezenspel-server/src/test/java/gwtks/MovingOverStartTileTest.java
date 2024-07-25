package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.*;
import static gwtks.MoveResult.CAN_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.*;

class MovingOverStartTileTest {
    private MoveMessage moveMessage = new MoveMessage();
    private MoveResponse moveResponse = new MoveResponse();

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

    // moving
    @Test
    void passingStartTile1_0Forward_NotPossibleWhenPlayerIsThere_PawnMovesBackwards(){
        // when the pawn is on position 15 and takes two steps it will end up at position 13

        // GIVEN
        Card card = givePlayerCard(0,2);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,15));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(1,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response is correct
        assertEquals(new TileId(0,13), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN GameState is correct
        assertEquals(new TileId(0,13), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void movingOverStartTile0_0Forwards_NotPossibleWhenPlayerIsThere_PawnMovesBackwards(){
        // when the pawn is on position 15 and takes two steps it will end up at position 13

        // GIVEN
        Card card = givePlayerCard(1,12);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,0));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(7,10));

        // WHEN
        createMoveMessage(moveMessage, pawn2, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response is correct
        assertEquals(new TileId(7,8), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN GameState is correct
        assertEquals(new TileId(7,8), GameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void passingStartTile1_0Forward_NotPossibleWhenPlayerIsThere_PawnMovesForwardAndBack(){
        // when the pawn is on position 14 and takes 5 steps it will end up at position 11

        // GIVEN
        Card card = givePlayerCard(0,5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,14));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(1,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals( new TileId(0,11), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN GameState is correct
        assertEquals(new TileId(0,11), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void passingStartTile0_0Backwards_NotPossibleWhenPlayerIsThere_PawnMovesForward(){
        // when the pawn is on position 14 and takes 5 steps it will end up at position 11

        // GIVEN
        Card card = givePlayerCard(1,-5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,0));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(0,3));

        // WHEN
        createMoveMessage(moveMessage, pawn2, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals( new TileId(0,4), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN GameState is correct
        assertEquals(new TileId(0,4), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void endUpOnStartTile0_0Backwards_MoveForward(){
        // when the pawn is on position 14 and takes 5 steps it will end up at position 11

        // GIVEN
        Card card = givePlayerCard(1,-3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,0));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(0,3));

        // WHEN
        createMoveMessage(moveMessage, pawn2, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response msg is correct
        assertEquals( new TileId(0,2), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN GameState is correct
        assertEquals(new TileId(0,2), GameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void passingStartTile1_0Backward_NotPossibleWhenPlayerIsThere_PawnMovesForward(){
        // when the pawn is on position 2 and takes -4 steps it will end up at position 4

        // GIVEN
        Card card = givePlayerCard(0,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(1,2));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, new TileId(1,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(1,4), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,4), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void passingStartTile1_0Backward_PossibleWhenOtherPlayerIsThere_PawnMovesBackward(){
        // GIVEN
        Card card = givePlayerCard(1,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,2));
        Pawn pawn2 = createPawnAndPlaceOnBoard(0, new TileId(1,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId(0,14), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,14), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void PawnMovesBackward_EndsOnStart(){
        // GIVEN
        Card card = givePlayerCard(1,-1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,1));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId(1,0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,0), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void PawnMovesForward_EndsOnStart(){
        // GIVEN
        Card ace = givePlayerAce(1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,15));

        // WHEN
        createMoveMessage(moveMessage, pawn1, ace);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(2,0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(2,0), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnOnNestTile_DoesNotMove(){
        // GIVEN
        Card card = givePlayerCard(0,10);
        Pawn pawn1 = createPawnAndPlaceOnBoard(1, new TileId(1,-2));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(1,-2), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void PawnMovesBackwardFromStartTile1_0ToPreviousSection(){
        // GIVEN
        Card card = givePlayerCard(0,-1);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(1,0));

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
    void PawnMovesBackwardFromStartTile0_0ToPreviousSection(){
        // GIVEN
        Card card = givePlayerCard(0,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0, new TileId(0,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(7,12), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(7,12), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnEndOnOccupiedStartTileBackwards_Blockade_EndsOnTile2(){
        // GIVEN
        Card card = givePlayerCard(0,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,4));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,2), new TileId(0,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId(0,2), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(0,2), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnEndOnOccupiedStartTileBackwards_NoBlockade_CannotMove(){
        // GIVEN
        Card card = givePlayerCard(1,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(0,4));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,2), new TileId(0,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        // THEN Gamestate is correct
        assertEquals(new TileId(0,4), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnEndOnOccupiedStartTileBackwards_Tile2IsAlsoOccupied_CannotMove(){
        // GIVEN
        Card card = givePlayerCard(0,-4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,4));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,2), new TileId(0,0));
        Pawn pawn3 = createPawnAndPlaceOnBoard(new PawnId(0,3), new TileId(0,2));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1().getLast());
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        // THEN Gamestate is correct
        assertEquals(new TileId(0,4), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnEndOnOccupiedStartTileForwards_NoBlockade_CannotMove(){
        // GIVEN
        Card card = givePlayerCard(1,4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(7,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,2), new TileId(0,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        // THEN Gamestate is correct
        assertEquals(new TileId(7,12), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnEndOnOccupiedStartTileForwards_Blockade_EndsOnTile14(){
        // GIVEN
        Card card = givePlayerCard(1,4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(7,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,2), new TileId(0,0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId(7,14), moveResponse.getMovePawn1().getLast());
        // THEN Gamestate is correct
        assertEquals(new TileId(7,14), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnEndOnOccupiedStartTileForwards_Blockade_Tile14IsAlsoOccupied_CannotMove(){
        // GIVEN
        Card card = givePlayerCard(1,4);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(1,1), new TileId(7,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(0, new TileId(0,0));
        Pawn pawn3 = createPawnAndPlaceOnBoard(new PawnId(1,2), new TileId(7,14));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId(7,12), GameState.getPawn(pawn1).getCurrentTileId());
    }
}