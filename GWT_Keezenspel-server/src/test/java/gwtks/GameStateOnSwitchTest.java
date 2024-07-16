package gwtks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameStateUtil.createPawnAndPlaceOnBoard;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStateOnSwitchTest {
    private MoveMessage moveMessage = new MoveMessage();
    private MoveResponse moveResponse = new MoveResponse();

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
    void switchPawnsOnNormalTiles_SelectedOwnPawnFirst() {
        // GIVEN
        int playerId = 0;
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, playerId);
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
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, 1);
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
        TileId tileId1 = new TileId(playerId, 5);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, 2); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
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
        TileId tileId1 = new TileId(playerId, -1);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, playerId); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
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
        TileId tileId1 = new TileId(playerId, 16);
        TileId tileId2 = new TileId(1, 3);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, playerId); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
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
        TileId tileId1 = new TileId(playerId, 4);
        TileId tileId2 = new TileId(1, 0);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, playerId); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
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
        TileId tileId1 = new TileId(playerId, 0);
        TileId tileId2 = new TileId(1, 5);
        Pawn pawn1 = createPawnAndPlaceOnBoard(playerId, tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(1, tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, playerId); // request is made from unrelated player
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
        TileId tileId1 = new TileId(playerId, 4);
        TileId tileId2 = new TileId(playerId, 2);
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(playerId,0), tileId1);
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(playerId, 1), tileId2);

        // WHEN
        createSwitchMessage(pawn1, pawn2, playerId); // request is made from unrelated player
        GameState.processOnSwitch(moveMessage, moveResponse);

        // THEN: response message is correct
        assertNull(moveResponse.getMovePawn1());
        assertNull(moveResponse.getMovePawn2());
        // THEN: GameState is correct
        assertEquals(tileId1, GameState.getPawn(pawn1).getCurrentTileId());
        assertEquals(tileId2, GameState.getPawn(pawn2).getCurrentTileId());
    }


    private void createSwitchMessage(Pawn pawn1, Pawn pawn2, int playerId){
        moveMessage.setPlayerId(playerId);
        moveMessage.setPawnId1(pawn1.getPawnId());
        moveMessage.setPawnId2(pawn2.getPawnId());
        moveMessage.setMoveType(MoveType.SWITCH);
        moveMessage.setMoveType(MoveType.MOVE);
    }
}