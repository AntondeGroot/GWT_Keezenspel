package ADG.Games.Keezen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static ADG.Games.Keezen.GameStateUtil.*;
import static ADG.Games.Keezen.MessageType.CHECK_MOVE;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovingAndKillTest {
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

    }

    @Test
    void KillPawnOnNormalTile_Forward(){
        // GIVEN
        Card card = givePlayerCard(0,1);
        Pawn pawn1 = GameStateUtil.placePawnOnNest("0", new TileId("0",9));
        Pawn pawn2 = placePawnOnNest("1", new TileId("0",10));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("0",10), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",10), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnNormalTile_Backward(){
        // GIVEN
        Card card = givePlayerCard(0,-1);
        Pawn pawn1 = placePawnOnNest("0", new TileId("0",11));
        Pawn pawn2 = placePawnOnNest("1", new TileId("0",10));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",10));
        expectedMovement.add(pawn2.getNestTileId());

        // THEN response message is correct
        assertEquals(new TileId("0",10), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(expectedMovement, moveResponse.getMoveKilledPawn1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",10), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnOtherStartTile_Forward(){
        // GIVEN
        Card card = givePlayerCard(0,1);
        Pawn pawn1 = placePawnOnNest("0", new TileId("0",15));
        Pawn pawn2 = placePawnOnNest("2", new TileId("1",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",0), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnOtherStartTile_Backward(){
        // GIVEN
        Card card = givePlayerCard(0,-1);
        Pawn pawn1 = placePawnOnNest("0", new TileId("1",1));
        Pawn pawn2 = placePawnOnNest("2", new TileId("1",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",0), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnSection_Forward(){
        // GIVEN
        Card card = givePlayerCard(0,12);
        Pawn pawn1 = placePawnOnNest("0", new TileId("0",9));
        Pawn pawn2 = placePawnOnNest("2", new TileId("1",5));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",5), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",5), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnSection_Backward(){
        // GIVEN
        Card card = givePlayerCard(0,-12);
        Pawn pawn1 = placePawnOnNest("0", new TileId("1",5));
        Pawn pawn2 = placePawnOnNest("2", new TileId("0",9));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        GameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("0",9), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",9), GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), GameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void killPawnWith7CardPawn1(){
        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("0",14));
        Pawn otherPawn1 = placePawnOnBoard(new PawnId("1", 1),new TileId("0",3));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 3, pawn2,4, new Card(0,7));
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(otherPawn1.getNestTileId(), GameState.getPawn(otherPawn1).getCurrentTileId());
        assertEquals(otherPawn1.getPawnId(), moveResponse.getPawnIdKilled1());
        assertNull(moveResponse.getPawnIdKilled2());
    }
    @Test
    void killPawnWith7CardPawn2(){
        // GIVEN
        givePlayerSeven(0);
        Pawn pawn1 = placePawnOnBoard(new PawnId("0", 1),new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(new PawnId("0", 2),new TileId("0",6));
        Pawn otherPawn1 = placePawnOnBoard(new PawnId("1", 1),new TileId("0",8));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 5, pawn2,2, new Card(0,7));
        GameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(otherPawn1.getNestTileId(), GameState.getPawn(otherPawn1).getCurrentTileId());
        assertEquals(otherPawn1.getPawnId(), moveResponse.getPawnIdKilled2());
        assertNull(moveResponse.getPawnIdKilled1());
    }
}