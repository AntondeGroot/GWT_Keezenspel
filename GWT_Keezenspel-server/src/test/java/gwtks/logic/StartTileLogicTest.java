package gwtks.logic;

import gwtks.GameState;
import gwtks.Pawn;
import gwtks.PawnId;
import gwtks.TileId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gwtks.GameState.getPawn;
import static gwtks.GameStateUtil.createPawnAndPlaceOnBoard;
import static org.junit.jupiter.api.Assertions.*;

class StartTileLogicTest {
    TileId startTileId;

    @BeforeEach
    void setup(){
        GameState gameState = new GameState(3);
        startTileId = new TileId(0, 0);
    }

    @AfterEach
    void tearDown(){
        GameState.tearDown();
    }

    @Test
    void player2_canPassPlayer2_OnStartTile0(){
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(2,1), new TileId(2,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(2,2), new TileId(0,0));

        assertTrue(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
    @Test
    void player2_canPassPlayer1_OnStartTile0(){
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(2,1), new TileId(2,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(1,2), new TileId(0,0));

        assertTrue(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
    @Test
    void player2_cannotPassPlayer0_OnStartTile0(){
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(2,1), new TileId(2,12));
        Pawn pawn2 = createPawnAndPlaceOnBoard(new PawnId(0,1), new TileId(0,0));

        assertFalse(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
    @Test
    void player2_canPassEmptyStartTile0(){
        Pawn pawn1 = createPawnAndPlaceOnBoard(new PawnId(2,1), new TileId(2,12));

        assertTrue(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
}