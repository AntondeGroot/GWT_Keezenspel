package ADG.Games.Keezen.UnitTests;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.*;
import static com.adg.openapi.model.MoveResult.CAN_MAKE_MOVE;
import static com.adg.openapi.model.MoveType.MOVE;
import static com.adg.openapi.model.TempMessageType.CHECK_MOVE;
import static com.adg.openapi.model.TempMessageType.MAKE_MOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.CardsDeckMock;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveRequest;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.PawnId;
import com.adg.openapi.model.PositionKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameStateVersionTest {

  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setUp() {
    GameSession engine = new GameSession(new CardsDeckMock());
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();
    createGame_With_NPlayers(gameState, 3);
  }

  @AfterEach
  void tearDown() {
    gameState.tearDown();
    cardsDeck.reset();
    gameState.stop();
  }

  @Test
  void makeMove_MOVE_incrementsVersion() {
    long versionBefore = gameState.getVersion();

    Pawn pawn = placePawnOnBoard(gameState, new PawnId("0", 0), new PositionKey("0", 3));
    Card card = givePlayerCard(cardsDeck, 0, 5);

    MoveRequest request = new MoveRequest();
    createMoveRequest(request, pawn, card);
    MoveResponse response = new MoveResponse();
    gameState.processOnMove(request, response);

    assertEquals(CAN_MAKE_MOVE, response.getResult());
    assertEquals(versionBefore + 1, gameState.getVersion());
  }

  @Test
  void makeMove_SWITCH_incrementsVersion() {
    long versionBefore = gameState.getVersion();

    Pawn pawn0 = placePawnOnBoard(gameState, new PawnId("0", 0), new PositionKey("0", 3));
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("1", 0), new PositionKey("1", 3));
    Card jack = givePlayerJack(cardsDeck, 0);

    MoveRequest request = new MoveRequest();
    createSwitchMessage(request, pawn0, pawn1, jack);
    MoveResponse response = new MoveResponse();
    gameState.processOnSwitch(request, response);

    assertEquals(CAN_MAKE_MOVE, response.getResult());
    assertEquals(versionBefore + 1, gameState.getVersion());
  }

  @Test
  void makeMove_ONBOARD_incrementsVersion() {
    // pawn in nest (tileNr < 0)
    Pawn pawn = placePawnOnBoard(gameState, new PawnId("0", 0), new PositionKey("0", -1));
    Card ace = givePlayerAce(cardsDeck, 0);

    long versionBefore = gameState.getVersion();

    MoveRequest request = new MoveRequest();
    request.setPlayerId("0");
    request.setPawn1Id(pawn.getPawnId());
    request.setCardId(ace.getUuid());
    request.setTempMessageType(MAKE_MOVE);
    MoveResponse response = new MoveResponse();
    gameState.processOnBoard(request, response);

    assertEquals(CAN_MAKE_MOVE, response.getResult());
    assertEquals(versionBefore + 1, gameState.getVersion());
  }

  @Test
  void makeMove_SPLIT_incrementsVersion() {
    long versionBefore = gameState.getVersion();

    Pawn pawn0 = placePawnOnBoard(gameState, new PawnId("0", 0), new PositionKey("0", 3));
    Pawn pawn1 = placePawnOnBoard(gameState, new PawnId("0", 1), new PositionKey("0", 5));
    Card seven = givePlayerSeven(cardsDeck, 0);

    MoveRequest request = new MoveRequest();
    createSplitMessage(request, pawn0, 3, pawn1, 4, seven);
    MoveResponse response = new MoveResponse();
    gameState.processOnSplit(request, response);

    assertEquals(CAN_MAKE_MOVE, response.getResult());
    assertEquals(versionBefore + 1, gameState.getVersion());
  }

  @Test
  void checkMove_MOVE_doesNotIncrementVersion() {
    long versionBefore = gameState.getVersion();

    Pawn pawn = placePawnOnBoard(gameState, new PawnId("0", 0), new PositionKey("0", 3));
    Card card = givePlayerCard(cardsDeck, 0, 5);

    MoveRequest request = new MoveRequest();
    createMoveRequest(request, pawn, card);
    request.setTempMessageType(CHECK_MOVE); // override to check-only

    MoveResponse response = new MoveResponse();
    gameState.processOnMove(request, response, false);

    assertEquals(CAN_MAKE_MOVE, response.getResult());
    assertEquals(versionBefore, gameState.getVersion());
  }
}