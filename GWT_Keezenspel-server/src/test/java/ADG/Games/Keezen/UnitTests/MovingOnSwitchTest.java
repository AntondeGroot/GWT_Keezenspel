package ADG.Games.Keezen.UnitTests;

import com.adg.openapi.model.Card;
import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import ADG.Games.Keezen.Move.MessageType;
import ADG.Games.Keezen.Move.MoveMessage;
import ADG.Games.Keezen.Move.MoveResponse;
import ADG.Games.Keezen.Move.MoveResult;
import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.TileId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static ADG.Games.Keezen.Move.MessageType.MAKE_MOVE;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MovingOnSwitchTest {
    private MoveMessage moveMessage = new MoveMessage();
    private MoveResponse moveResponse = new MoveResponse();

    private GameSession engine;
    private GameState gameState;
    private CardsDeckInterface cardsDeck;

    @BeforeEach
    void setUp() {
        engine = new GameSession();
        gameState = engine.getGameState();
        cardsDeck = engine.getCardsDeck();

        createGame_With_NPlayers(gameState, 3);
        moveMessage = new MoveMessage();
        moveResponse = new MoveResponse();
    }

    @AfterEach
    void tearDown() {
        gameState.tearDown();
        moveMessage = null;
        moveResponse = null;
        cardsDeck.reset();
    }

    @Test
    void playerPlaysJack_ThenNextPlayerPlays() {
        // GIVEN
        Card card = givePlayerJack(cardsDeck , 0);
        String playerId = "0";
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId("1", 3);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals("1", gameState.getPlayerIdTurn());
    }
    @Test
    void playerPlaysJackAsLastCard_ThenNextPlayerPlays() {
        // GIVEN
        String playerId = "0";
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId("1", 3);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        for (int i = 0; i < 4; i++) {
            sendValidMoveMessage(gameState, cardsDeck, "0");
            sendValidMoveMessage(gameState, cardsDeck, "1");
            sendValidMoveMessage(gameState, cardsDeck, "2");
        }

        Card card = givePlayerJack(cardsDeck , 0);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN
        assertEquals("1", gameState.getPlayerIdTurn());
    }

    @Test
    void switchPawnsOnNormalTiles_SelectedOwnPawnFirst() {
        // GIVEN
        Card card = givePlayerJack(cardsDeck , 0);
        String playerId = "0";
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId("1", 3);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MAKE_MOVE, moveResponse.getMessageType());
        assertEquals(tileId2, moveResponse.getMovePawn1().getLast());
        assertEquals(tileId1, moveResponse.getMovePawn2().getLast());
        // THEN: GameState is correct
        assertEquals(tileId2, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId1, gameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void switchPawnsOnNormalTiles_SelectedOtherPawnFirst() {
        // GIVEN
        String playerId = "0";
        Card card = givePlayerJack(cardsDeck , 0);
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId("1", 3);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MAKE_MOVE, moveResponse.getMessageType());
        assertEquals(tileId2, moveResponse.getMovePawn1().getLast());
        assertEquals(tileId1, moveResponse.getMovePawn2().getLast());
        // THEN: GameState is correct
        assertEquals(tileId2, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId1, gameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void doNotSwitchPawnsBelongingToOthers() {
        // GIVEN
        String playerId = "0";
        Card card = givePlayerJack(cardsDeck , 0);
        Card card2 = givePlayerJack(cardsDeck , 2);
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId("1", 3);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        moveMessage.setPlayerId("2"); // request is made from unrelated player
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantSwitchPawnFromNest() {
        // GIVEN
        String playerId = "0";
        Card card = givePlayerJack(cardsDeck , 0);
        TileId tileId1 = new TileId(playerId, -1);
        TileId tileId2 = new TileId("1", 3);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantSwitchPawnFromFinish() {
        // GIVEN
        String playerId = "0";
        Card card = givePlayerJack(cardsDeck , 0);
        TileId tileId1 = new TileId(playerId, 16);
        TileId tileId2 = new TileId("1", 3);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantTakeOtherPawnFromStart() {
        // GIVEN
        String playerId = "0";
        Card card = givePlayerJack(cardsDeck , 0);
        TileId tileId1 = new TileId(playerId, 4);
        TileId tileId2 = new TileId("1", 0);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void canSwitchPawnFromOwnStart() {
        // GIVEN
        String playerId = "0";
        Card card = givePlayerJack(cardsDeck , 0);
        TileId tileId1 = new TileId(playerId, 0);
        TileId tileId2 = new TileId("1", 5);
        Pawn pawn1 = placePawnOnNest(gameState , playerId, tileId1);
        Pawn pawn2 = placePawnOnNest(gameState , "1", tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MAKE_MOVE, moveResponse.getMessageType());
        assertEquals(tileId2, moveResponse.getMovePawn1().getLast());
        assertEquals(tileId1, moveResponse.getMovePawn2().getLast());
        // THEN: GameState is correct
        assertEquals(tileId2, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId1, gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantSwitchWithOwnPawn() {
        // GIVEN
        String playerId = "0";
        Card card = givePlayerJack(cardsDeck , Integer.valueOf(playerId));
        TileId tileId1 = new TileId(playerId, 4);
        TileId tileId2 = new TileId(playerId, 2);
        Pawn pawn1 = placePawnOnBoard(gameState , new PawnId(playerId,0), tileId1);
        Pawn pawn2 = placePawnOnBoard(gameState , new PawnId(playerId, 1), tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        gameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, gameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, gameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void testWhenPawnsSwitch_CardGetsRemovedFromHand_AndNextPlayerPlays(){
        Card card = givePlayerJack(cardsDeck , 0);
        Pawn pawn1 = placePawnOnNest(gameState , "0", new TileId("0",12));
        Pawn pawn2 = placePawnOnNest(gameState , "1", new TileId("0",5));
        assertEquals(5, cardsDeck.getCardsForPlayer("0").size());

        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        gameState.processOnSwitch(moveMessage, moveResponse);

        assertEquals(4, cardsDeck.getCardsForPlayer("0").size());
        assertEquals("1", gameState.getPlayerIdTurn());
    }
    @Test
    void testingWhenPawnsSwitch_CardNotRemovedFromHand(){
        Card card = givePlayerJack(cardsDeck , 0);
        Pawn pawn1 = placePawnOnNest(gameState , "0", new TileId("0",12));
        Pawn pawn2 = placePawnOnNest(gameState , "1", new TileId("0",5));
        assertEquals(5, cardsDeck.getCardsForPlayer("0").size());

        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        gameState.processOnSwitch(moveMessage, moveResponse);

        assertEquals(5, cardsDeck.getCardsForPlayer("0").size());
        assertEquals("0",gameState.getPlayerIdTurn());
    }
}