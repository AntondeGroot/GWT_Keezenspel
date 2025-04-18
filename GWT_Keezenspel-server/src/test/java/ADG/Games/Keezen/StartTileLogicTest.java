package ADG.Games.Keezen;

import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.PawnId;
import ADG.Games.Keezen.logic.StartTileLogic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class StartTileLogicTest {
    TileId startTileId;

    @BeforeEach
    void setup(){

        createGame_With_NPlayers(3);
        startTileId = new TileId("0", 0);
    }

    @AfterEach
    void tearDown(){
        GameState.tearDown();
    }

    @Test
    void player2_canPassPlayer2_OnStartTile0(){
        Pawn pawn1 = GameStateUtil.placePawnOnBoard(new PawnId("2",1), new TileId("2",12));
        Pawn pawn2 = GameStateUtil.placePawnOnBoard(new PawnId("2",2), new TileId("0",0));

        Assertions.assertTrue(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
    @Test
    void player2_canPassPlayer1_OnStartTile0(){
        Pawn pawn1 = GameStateUtil.placePawnOnBoard(new PawnId("2",1), new TileId("2",12));
        Pawn pawn2 = GameStateUtil.placePawnOnBoard(new PawnId("1",2), new TileId("0",0));

        assertTrue(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
    @Test
    void player2_cannotPassPlayer0_OnStartTile0(){
        Pawn pawn1 = GameStateUtil.placePawnOnBoard(new PawnId("2",1), new TileId("2",12));
        Pawn pawn2 = GameStateUtil.placePawnOnBoard(new PawnId("0",1), new TileId("0",0));

        assertFalse(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
    @Test
    void player2_canPassEmptyStartTile0(){
        Pawn pawn1 = GameStateUtil.placePawnOnBoard(new PawnId("2",1), new TileId("2",12));

        assertTrue(StartTileLogic.canPassStartTile(pawn1.getPawnId(), startTileId));
    }
}