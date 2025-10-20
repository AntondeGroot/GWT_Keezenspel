package ADG.Games.Keezen.UnitTests;

import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class StartTileLogicTest {

  PositionKey startTileId;

  private GameSession engine;
  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setup() {
    engine = new GameSession();
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();

    createGame_With_NPlayers(gameState, 3);
    startTileId = new PositionKey("0", 0);
  }

  @AfterEach
  void tearDown() {
    gameState.tearDown();
  }

  @Test
  void player2_canPassPlayer2_OnStartTile0() {
//        Pawn pawn1 = GameStateUtil.placePawnOnBoard(gameState , new PawnId("2",1), new PositionKey("2",12));
//        Pawn pawn2 = GameStateUtil.placePawnOnBoard(gameState , new PawnId("2",2), new PositionKey("0",0));
//
//        Assertions.assertTrue(gameState.canPassStartTile(pawn1.getPawnId(), startTileId));
    fail();
  }

  @Test
  void player2_canPassPlayer1_OnStartTile0() {
//        Pawn pawn1 = GameStateUtil.placePawnOnBoard(gameState , new PawnId("2",1), new PositionKey("2",12));
//        Pawn pawn2 = GameStateUtil.placePawnOnBoard(gameState , new PawnId("1",2), new PositionKey("0",0));
//
//        assertTrue(gameState.canPassStartTile(pawn1.getPawnId(), startTileId));
    fail();
  }

  @Test
  void player2_cannotPassPlayer0_OnStartTile0() {
//        Pawn pawn1 = GameStateUtil.placePawnOnBoard(gameState , new PawnId("2",1), new PositionKey("2",12));
//        Pawn pawn2 = GameStateUtil.placePawnOnBoard(gameState , new PawnId("0",1), new PositionKey("0",0));
//
//        assertFalse(gameState.canPassStartTile(pawn1.getPawnId(), startTileId));
    fail();
  }

  @Test
  void player2_canPassEmptyStartTile0() {
//        Pawn pawn1 = GameStateUtil.placePawnOnBoard(gameState , new PawnId("2",1), new PositionKey("2",12));
//
//        assertTrue(gameState.canPassStartTile(pawn1.getPawnId(), startTileId));
    fail();
  }
}