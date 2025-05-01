package ADG.Games.Keezen.UnitTests;

import ADG.Games.Keezen.Cards.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.TileId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovingAndKillTest {
    MoveMessage moveMessage = new MoveMessage();
    MoveResponse moveResponse = new MoveResponse();

    private GameState gameState;
    private CardsDeckInterface cardsDeck;

    @BeforeEach
    void setUp() {
        GameSession engine = new GameSession();
        gameState = engine.getGameState();
        cardsDeck = engine.getCardsDeck();

        createGame_With_NPlayers(gameState, 8);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        gameState.tearDown();
        moveMessage = null;
        moveResponse = null;

    }

    @Test
    void KillPawnOnNormalTile_Forward(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, 1);
        Pawn pawn1 = GameStateUtil.placePawnOnNest(gameState , "0", new TileId("0",9));
        Pawn pawn2 = placePawnOnNest(gameState, "1", new TileId("0",10));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("0",10), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",10), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnNormalTile_Backward(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, -1);
        Pawn pawn1 = placePawnOnNest(gameState , "0", new TileId("0",11));
        Pawn pawn2 = placePawnOnNest(gameState , "1", new TileId("0",10));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN
        LinkedList<TileId> expectedMovement = new LinkedList<>();
        expectedMovement.add(new TileId("0",10));
        expectedMovement.add(pawn2.getNestTileId());

        // THEN response message is correct
        assertEquals(new TileId("0",10), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(expectedMovement, moveResponse.getMoveKilledPawn1());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",10), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnOtherStartTile_Forward(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, 1);
        Pawn pawn1 = placePawnOnNest(gameState , "0", new TileId("0",15));
        Pawn pawn2 = placePawnOnNest(gameState , "2", new TileId("1",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",0), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnOtherStartTile_Backward(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, -1);
        Pawn pawn1 = placePawnOnNest(gameState , "0", new TileId("1",1));
        Pawn pawn2 = placePawnOnNest(gameState , "2", new TileId("1",0));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",0), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",0), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnSection_Forward(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, 12);
        Pawn pawn1 = placePawnOnNest(gameState , "0", new TileId("0",9));
        Pawn pawn2 = placePawnOnNest(gameState , "2", new TileId("1",5));

        // WHEN
        createMoveMessage(moveMessage, pawn1, card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("1",5), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("1",5), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void KillPawnOnSection_Backward(){
        // GIVEN
        Card card = givePlayerCard(cardsDeck, 0, -12);
        Pawn pawn1 = placePawnOnNest(gameState , "0", new TileId("1",5));
        Pawn pawn2 = placePawnOnNest(gameState , "2", new TileId("0",9));

        // WHEN
        createMoveMessage(moveMessage, pawn1,card);
        gameState.processOnMove(moveMessage, moveResponse);

        // THEN response message is correct
        assertEquals(new TileId("0",9), moveResponse.getMovePawn1().getLast());  // moves the pawn to the correct tile
        assertEquals(pawn2.getNestTileId(), moveResponse.getMoveKilledPawn1().getLast());                          // moves the correct pawn
        // THEN Gamestate is correct
        assertEquals(new TileId("0",9), gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(pawn2.getNestTileId(), gameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void killPawnWith7CardPawn1(){
        // GIVEN
        givePlayerSeven(cardsDeck, 0);
        Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new TileId("0",14));
        Pawn otherPawn1 = placePawnOnBoard(gameState, new PawnId("1", 1), new TileId("0",3));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 3, pawn2,4, new Card(0,7));
        gameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(otherPawn1.getNestTileId(), gameState.getPawn(otherPawn1).getCurrentTileId());
        assertEquals(otherPawn1.getPawnId(), moveResponse.getPawnIdKilled1());
        assertNull(moveResponse.getPawnIdKilled2());
    }
    @Test
    void killPawnWith7CardPawn2(){
        // GIVEN
        givePlayerSeven(cardsDeck, 0);
        Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new TileId("0",0));
        Pawn pawn2 = placePawnOnBoard(gameState, new PawnId("0", 2), new TileId("0",6));
        Pawn otherPawn1 = placePawnOnBoard(gameState, new PawnId("1", 1), new TileId("0",8));

        // WHEN no decision was made how to split the 7 among the two pawns
        createSplitMessage(moveMessage, pawn1, 5, pawn2,2, new Card(0,7));
        gameState.processOnSplit(moveMessage, moveResponse);

        // THEN
        assertEquals(otherPawn1.getNestTileId(), gameState.getPawn(otherPawn1).getCurrentTileId());
        assertEquals(otherPawn1.getPawnId(), moveResponse.getPawnIdKilled2());
        assertNull(moveResponse.getPawnIdKilled1());
    }
}