package ADG.Games.Keezen;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.Move.MoveResult.CANNOT_MAKE_MOVE;
import static ADG.Games.Keezen.Move.MoveResult.CAN_MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.*;

class MovingOnFinishTilesTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();

    @BeforeEach
    void setUp() {
        createGame_With_NPlayers(8);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        GameState.tearDown();
        moveMessage = null;
        moveResponse = null;
        ADG.Games.Keezen.CardsDeck.reset();
    }

    @Test
    void moveFromLastSectionOntoFinish(){
        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = GameStateUtil.placePawnOnNest("1", new TileId("0",14));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId("1",17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveFurtherDownFinishTile(){
        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = placePawnOnNest("1", new TileId("1",16));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId("1",19), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",19), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveBackwardsOnFinishTiles_NegativeSteps(){
        // GIVEN
        Card card = givePlayerCard(1,-2);
        Pawn pawn1 = placePawnOnNest("1", new TileId("1",19));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveBackwardsOnFinishTiles_NegativeSteps_NotEndposition(){
        // GIVEN
        Card card = givePlayerCard(1,-1);
        Pawn pawn1 = placePawnOnNest("1", new TileId("1",18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",17), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void moveBackwardsOnFinishTiles_PositiveSteps(){
        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = placePawnOnNest("1", new TileId("1",18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId("1",17), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",17), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void MoveOutOfFinishTileWhenAlreadyOnFinishTile_ByMovingBackwards(){
        // GIVEN
        Card card = givePlayerCard(1,-4);
        Pawn pawn1 = placePawnOnNest("1", new TileId("1",19));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId("0",15), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",15), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOutOfFinishTileWhenAlreadyOnFinishTile_ByMovingForwards(){
        // GIVEN
        Card card = givePlayerCard(1,4);
        Pawn pawn1 = placePawnOnNest("1", new TileId("1",19));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("0",15), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",15), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButRoomToMove_MoveBackwards(){
        // GIVEN
        Card card = givePlayerCard(1,-5);
        Pawn pawn1 = placePawnOnBoard(new PawnId("1",0), new TileId("1",19));
        Pawn pawn2 = placePawnOnBoard(new PawnId("1",1), new TileId("1",16));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",18), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void MoveOnFinishTilesBackAndForthWhenAlreadyOnFinishTileAndBlockedByOwnPawns_ButRoomToMove_MoveForwards(){
        // pawn - x - x - pawn1
        // GIVEN
        Card card = givePlayerCard(1,5);
        Pawn pawn1 = placePawnOnBoard(new PawnId("1",0), new TileId("1",19));
        Pawn pawn2 = placePawnOnBoard(new PawnId("1",1), new TileId("1",16));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",18), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn1.getPawnId(), moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void whenPawnClosedInAtTile19_PositiveSteps_DontMove(){
        // x - x - pawn - pawn1
        // GIVEN
        Card card = givePlayerCard(1,5);
        Pawn pawn1 = placePawnOnBoard(new PawnId("1",0), new TileId("1",19));
        Pawn pawn2 = placePawnOnBoard(new PawnId("1",1), new TileId("1",18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId("1",19), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void whenPawnClosedInAtTile18_DontMove(){
        // x - x - pawn - pawn1
        // GIVEN
        Card card = givePlayerCard(1,5);
        Pawn pawn2 = placePawnOnBoard(new PawnId("1",0), new TileId("1",19));
        Pawn pawn1 = placePawnOnBoard(new PawnId("1",1), new TileId("1",18));
        Pawn pawn3 = placePawnOnBoard(new PawnId("1",2), new TileId("1",17));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId("1",18), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void whenPawnClosedInAtTile19_NegativeSteps_DontMove(){
        // x - x - pawn - pawn1
        // GIVEN
        Card card = givePlayerCard(1,-5);
        Pawn pawn1 = placePawnOnBoard(new PawnId("1",0), new TileId("1",19));
        Pawn pawn2 = placePawnOnBoard(new PawnId("1",1), new TileId("1",18));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);
        System.out.println(CardsDeck.getCardsForPlayer("1").toString());

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());  // moves the pawn to the correct tile
        assertNull(moveResponse.getPawnId1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",19), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnAt14_15_17_When14Takes3Steps_CannotMove(){
        // it would otherwise end up on 0,15 and be placed on its own pawn
        // GIVEN
        Card card = givePlayerCard(1,3);
        Pawn pawn1 = placePawnOnBoard(new PawnId("1",1), new TileId("0",14));
        Pawn pawn2 = placePawnOnBoard(new PawnId("1",2), new TileId("0",15));
        Pawn pawn3 = placePawnOnBoard(new PawnId("1",3), new TileId("1",17));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId("0",14), GameState.getPawn(pawn1).getCurrentTileId());
    }
    @Test
    void pawnAt14_18_When18Takes4StepsBack_CannotMove(){
        // it would otherwise end up on 0,14 and be placed on its own pawn
        // GIVEN
        Card card = givePlayerCard(1,-4);
        Pawn pawn1 = placePawnOnBoard(new PawnId("1",1), new TileId("1",18));
        Pawn pawn2 = placePawnOnBoard(new PawnId("1",2), new TileId("0",14));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getPawnId1());
        // THEN Gamestate is correct
        assertEquals(new TileId("1",18), GameState.getPawn(pawn1).getCurrentTileId());
    }

    @Test
    void pawnMoves_ToFinish_AnimateToRightTile_BugFix() {
        /**
         */

        // setup
        GameState.tearDown();
        createGame_With_NPlayers(3);

        // GIVEN
        Card card = givePlayerCard(2,10);
        Pawn pawn1 = placePawnOnBoard(new PawnId("2",1) ,new TileId("1",7));
        /* The existence of the second pawn at a position of >7 causes the first pawn to not go into the finish and moves to (1,3)*/
        Pawn pawn2 = placePawnOnBoard(new PawnId("2",2) ,new TileId("2",8));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        assertEquals(CAN_MAKE_MOVE, moveResponse.getResult());
        assertEquals(new TileId("2",17), moveResponse.getMovePawn1().getLast());
    }
}