package ADG.Games.Keezen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.GameStateUtil.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MovingOnSwitchTest {
    private MoveMessage moveMessage = new MoveMessage();
    private MoveResponse moveResponse = new MoveResponse();

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
    void switchPawnsOnNormalTiles_SelectedOwnPawnFirst() {
        // GIVEN
        Card card = givePlayerJack(0);
        int playerId = 0;
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(tileId2, moveResponse.getMovePawn1().getLast());
        assertEquals(tileId1, moveResponse.getMovePawn2().getLast());
        // THEN: GameState is correct
        assertEquals(tileId2, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId1, GameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void switchPawnsOnNormalTiles_SelectedOtherPawnFirst() {
        // GIVEN
        int playerId = 0;
        Card card = givePlayerJack(0);
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(tileId2, moveResponse.getMovePawn1().getLast());
        assertEquals(tileId1, moveResponse.getMovePawn2().getLast());
        // THEN: GameState is correct
        assertEquals(tileId2, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId1, GameState.getPawn(pawn2).getCurrentTileId());
    }

    @Test
    void doNotSwitchPawnsBelongingToOthers() {
        // GIVEN
        int playerId = 0;
        Card card = givePlayerJack(0);
        Card card2 = givePlayerJack(2);
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        moveMessage.setPlayerId(2); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantSwitchPawnFromNest() {
        // GIVEN
        int playerId = 0;
        Card card = givePlayerJack(0);
        TileId tileId1 = new TileId(playerId, -1);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantSwitchPawnFromFinish() {
        // GIVEN
        int playerId = 0;
        Card card = givePlayerJack(0);
        TileId tileId1 = new TileId(playerId, 16);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantTakeOtherPawnFromStart() {
        // GIVEN
        int playerId = 0;
        Card card = givePlayerJack(0);
        TileId tileId1 = new TileId(playerId, 4);
        TileId tileId2 = new TileId(1, 0);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void canSwitchPawnFromOwnStart() {
        // GIVEN
        int playerId = 0;
        Card card = givePlayerJack(0);
        TileId tileId1 = new TileId(playerId, 0);
        TileId tileId2 = new TileId(1, 5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(tileId2, moveResponse.getMovePawn1().getLast());
        assertEquals(tileId1, moveResponse.getMovePawn2().getLast());
        // THEN: GameState is correct
        assertEquals(tileId2, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId1, GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void cantSwitchWithOwnPawn() {
        // GIVEN
        int playerId = 0;
        Card card = givePlayerJack(playerId);
        TileId tileId1 = new TileId(playerId, 4);
        TileId tileId2 = new TileId(playerId, 2);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(playerId,0), tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(playerId, 1), tileId2);

        // WHEN
        createSwitchMessage(moveMessage, pawn1, pawn2, card); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertEquals(MoveResult.CANNOT_MAKE_MOVE, moveResponse.getResult());
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, GameState.getPawn(pawn2).getCurrentTileId());
    }
    @Test
    void testWhenPawnsSwitch_CardGetsRemovedFromHand_AndNextPlayerPlays(){
        Card card = givePlayerJack(0);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(0,5));
        assertEquals(5, ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).size());

        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        GameState.processOnSwitch(moveMessage, moveResponse);

        assertEquals(4, ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).size());
        assertEquals(1,GameState.getPlayerIdTurn());
    }
    @Test
    void testingWhenPawnsSwitch_CardNotRemovedFromHand(){
        Card card = givePlayerJack(0);
        Pawn pawn1 = createPawnAndPlaceOnBoard(0,new TileId(0,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(1,new TileId(0,5));
        assertEquals(5, ADG.Games.Keezen.CardsDeck.getCardsForPlayer(0).size());

        createSwitchMessage(moveMessage, pawn1, pawn2, card);
        moveMessage.setMessageType(MessageType.CHECK_MOVE);
        GameState.processOnSwitch(moveMessage, moveResponse);

        assertEquals(5, CardsDeck.getCardsForPlayer(0).size());
        assertEquals(0,GameState.getPlayerIdTurn());
    }
}