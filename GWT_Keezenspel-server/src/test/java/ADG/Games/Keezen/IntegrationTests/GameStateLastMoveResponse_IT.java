package ADG.Games.Keezen.IntegrationTests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ADG.Games.Keezen.ApiUtils.ApiUtil;
import ADG.Games.Keezen.utils.ApiCallsHelper;
import ADG.Games.Keezen.utils.BaseUnitTest;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that lastMoveResponse is correctly set in the game state after a move,
 * and cleared after a forfeit.
 *
 * These tests exist to catch the following bugs:
 * 1. GameStatesApiDelegateImpl not including lastMoveResponse in its response,
 *    causing opponent pawns to teleport instead of animate.
 * 2. CardsApiDelegateImpl not clearing lastMoveResponse on forfeit,
 *    causing the last move animation to replay after a forfeit.
 */
public class GameStateLastMoveResponse_IT extends BaseUnitTest {

  private static final int CARD_VALUE = 5;

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();
  private String sessionId;
  private String player0Id;
  private String player1Id;

  @BeforeEach
  void setUp() {
    sessionId = ApiUtil.createStandardGame();
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
    // Place pawn on board so it can move (tileNr >= 0 means on board)
    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player0Id, 3);
    // Give player 0 a specific card — uuid == cardValue (see TestResetController)
    ApiUtil.setCardForPlayer(sessionId, player0Id, CARD_VALUE);
  }

  @AfterEach
  void tearDown() {
    apiHelper.stopGame(sessionId);
  }

  private Map<String, Object> buildMoveRequest(String playerId, int pawnNr, int cardUuid, int steps) {
    return Map.of(
        "playerId", playerId,
        "cardId", cardUuid,
        "pawn1Id", Map.of("playerId", playerId, "pawnNr", pawnNr),
        "stepsPawn1", steps,
        "tempMessageType", "MAKE_MOVE"
    );
  }

  @Test
  void afterMove_gameState_includesLastMoveResponse() {
    // GIVEN: player 0's pawn is on the board with a card (done in setUp)
    Map<String, Object> moveRequest = buildMoveRequest(player0Id, 0, CARD_VALUE, CARD_VALUE);

    // WHEN: player 0 makes a move
    apiHelper.makeMove(sessionId, player0Id, moveRequest);

    // THEN: the game state response must contain a lastMoveResponse so that
    //       opponent clients can animate the move instead of teleporting the pawn
    Map<String, Object> gameState = apiHelper.getGameState(sessionId);
    assertNotNull(
        gameState.get("lastMoveResponse"),
        "lastMoveResponse should be present in game state after a move");
  }

  @Test
  void afterForfeit_gameState_lastMoveResponseIsNull() {
    // GIVEN: player 0 makes a move, which sets lastMoveResponse
    Map<String, Object> moveRequest = buildMoveRequest(player0Id, 0, CARD_VALUE, CARD_VALUE);
    apiHelper.makeMove(sessionId, player0Id, moveRequest);

    // verify precondition: lastMoveResponse is set
    assertNotNull(
        apiHelper.getGameState(sessionId).get("lastMoveResponse"),
        "Precondition failed: lastMoveResponse should be set after a move");

    // WHEN: player 1 forfeits (it is now player 1's turn after player 0 moved)
    ApiUtil.forfeitPlayerViaApi(sessionId, player1Id);

    // THEN: lastMoveResponse must be cleared so the animation does not replay
    //       on the next poll cycle
    Map<String, Object> gameState = apiHelper.getGameState(sessionId);
    assertNull(
        gameState.get("lastMoveResponse"),
        "lastMoveResponse should be null in game state after a forfeit");
  }
}