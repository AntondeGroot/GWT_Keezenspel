package adg.keezen.ApiTests;

import static org.junit.jupiter.api.Assertions.*;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

/**
 * API-level tests for the "mustPlayIfPossible" game option.
 *
 * <p>These tests hit the running server via HTTP. They verify that:
 * <ul>
 *   <li>With the option enabled, {@code DELETE /cards/{session}/{player}} (forfeit)
 *       returns 403 when the player has a valid move available.
 *   <li>With the option enabled, forfeit still succeeds when no move is possible.
 *   <li>Without the option, forfeit always succeeds regardless of board state.
 * </ul>
 */
class MustPlayIfPossibleApiTest extends BaseUnitTest {

  /**
   * mustPlayIfPossible=true, player has a regular card, pawn is on the board.
   * A valid move exists → forfeit must be rejected with 403 Forbidden.
   */
  @Test
  void forfeit_withMustPlay_moveAvailable_returns403() {
    String sessionId = ApiUtil.createGameWithMustPlay();
    String playerId = ApiUtil.getPlayerid(sessionId, 0);

    // Place pawn on normal board and give a card that can move it.
    ApiUtil.setPawnPosition(sessionId, playerId, 0, playerId, 5);
    ApiUtil.setCardForPlayer(sessionId, playerId, 5);

    assertThrows(
        HttpClientErrorException.Forbidden.class,
        () -> ApiUtil.forfeitPlayerViaApi(sessionId, playerId));
  }

  /**
   * mustPlayIfPossible=true, player has only a regular card (dealt hand cleared),
   * all 4 pawns are in the nest. Regular cards can only move board pawns → no move
   * available → forfeit must succeed.
   *
   * <p>Uses {@code setOnlyCardForPlayer} to replace the entire dealt hand with a
   * single regular card, making the test deterministic.
   */
  @Test
  void forfeit_withMustPlay_allPawnsOnNest_regularCardOnly_noMoveAvailable_succeeds() {
    String sessionId = ApiUtil.createGameWithMustPlay();
    String playerId = ApiUtil.getPlayerid(sessionId, 0);

    // Clear the dealt hand and give only card 5; all 4 pawns stay in the nest.
    ApiUtil.setOnlyCardForPlayer(sessionId, playerId, 5);

    assertDoesNotThrow(() -> ApiUtil.forfeitPlayerViaApi(sessionId, playerId));
  }

  /**
   * mustPlayIfPossible=false (default), player has a card and a pawn on the board.
   * The option is disabled → forfeit must always succeed regardless of board state.
   */
  @Test
  void forfeit_withoutMustPlay_moveAvailable_succeeds() {
    String sessionId = ApiUtil.createStandardGame();
    String playerId = ApiUtil.getPlayerid(sessionId, 0);

    ApiUtil.setPawnPosition(sessionId, playerId, 0, playerId, 5);
    ApiUtil.setCardForPlayer(sessionId, playerId, 5);

    assertDoesNotThrow(() -> ApiUtil.forfeitPlayerViaApi(sessionId, playerId));
  }

  /**
   * Safety-valve: even when a move is theoretically available, the server must allow
   * forfeit once the 3-minute timeout has elapsed — so the game can never be permanently
   * stuck on a single player's turn.
   *
   * <p>The test backdates the blocked-since timestamp via the test endpoint (rather than
   * waiting 3 real minutes), then verifies that a second forfeit attempt succeeds.
   */
  @Test
  void forfeit_withMustPlay_timeoutElapsed_allowsForfeitDespiteMoveAvailable() {
    String sessionId = ApiUtil.createGameWithMustPlay();
    String playerId = ApiUtil.getPlayerid(sessionId, 0);

    ApiUtil.setPawnPosition(sessionId, playerId, 0, playerId, 5);
    ApiUtil.setCardForPlayer(sessionId, playerId, 5);

    // First attempt: move is available → 403, timer starts.
    assertThrows(
        HttpClientErrorException.Forbidden.class,
        () -> ApiUtil.forfeitPlayerViaApi(sessionId, playerId));

    // Simulate 3 minutes passing by backdating the blocked-since timestamp.
    ApiUtil.simulateMustPlayTimeout(sessionId);

    // Second attempt: timeout has elapsed → server allows forfeit.
    assertDoesNotThrow(() -> ApiUtil.forfeitPlayerViaApi(sessionId, playerId));
  }

  /**
   * mustPlayIfPossible=true, all 4 own pawns are in the finish lane.
   * Finish pawns are excluded from the "must-play" check for any card → no move available
   * → forfeit must succeed regardless of what cards the player holds.
   *
   * <p>This is the most reliable "no move" scenario for an API-level test: it does not
   * depend on the random cards dealt at game start, because finish pawns are exempt for
   * every card type (regular, Ace, King, Jack, 7).
   */
  @Test
  void forfeit_withMustPlay_allPawnsInFinish_noMoveAvailable_succeeds() {
    String sessionId = ApiUtil.createGameWithMustPlay();
    String playerId = ApiUtil.getPlayerid(sessionId, 0);

    ApiUtil.setPawnPosition(sessionId, playerId, 0, playerId, 16);
    ApiUtil.setPawnPosition(sessionId, playerId, 1, playerId, 17);
    ApiUtil.setPawnPosition(sessionId, playerId, 2, playerId, 18);
    ApiUtil.setPawnPosition(sessionId, playerId, 3, playerId, 19);

    assertDoesNotThrow(() -> ApiUtil.forfeitPlayerViaApi(sessionId, playerId));
  }
}