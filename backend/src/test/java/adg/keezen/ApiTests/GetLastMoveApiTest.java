package adg.keezen.ApiTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.ApiCallsHelper;
import adg.keezen.utils.BaseUnitTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

/**
 * API-level tests for {@code GET /moves/{sessionId}/last} (getLastMove).
 *
 * <p>Covers the three responses the delegate can return:
 * <ul>
 *   <li>404 when the game does not exist,
 *   <li>204 when the game exists but no move has been made yet,
 *   <li>200 with the MoveResponse after a move has been made.
 * </ul>
 */
class GetLastMoveApiTest extends BaseUnitTest {

  private static final int CARD_VALUE = 5;

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();

  @Test
  void getLastMove_unknownGame_returns404() {
    assertThrows(
        HttpClientErrorException.NotFound.class,
        () -> apiHelper.getLastMove("does-not-exist"));
  }

  @Test
  void getLastMove_gameStartedButNoMoveYet_returns204() {
    // GIVEN a started game in which nobody has moved yet
    String sessionId = ApiUtil.createStandardGame();

    // WHEN asking for the last move
    ResponseEntity<Map> response = apiHelper.getLastMove(sessionId);

    // THEN there is none
    assertEquals(
        204, response.getStatusCode().value(), "No move made yet should be 204 No Content");
    assertNull(response.getBody(), "204 response should have no body");

    apiHelper.stopGame(sessionId);
  }

  @Test
  void getLastMove_afterMove_returns200WithMoveResponse() {
    // GIVEN a started game where player 0 has a board pawn and a matching card
    String sessionId = ApiUtil.createStandardGame();
    String player0Id = ApiUtil.getPlayerid(sessionId, 0);
    ApiUtil.setPawnPosition(sessionId, player0Id, 0, player0Id, 3);
    ApiUtil.setCardForPlayer(sessionId, player0Id, CARD_VALUE);

    // WHEN player 0 makes a move (uuid == cardValue per TestResetController)
    Map<String, Object> moveRequest =
        Map.of(
            "playerId", player0Id,
            "cardId", CARD_VALUE,
            "pawn1Id", Map.of("playerId", player0Id, "pawnNr", 0),
            "stepsPawn1", CARD_VALUE,
            "tempMessageType", "MAKE_MOVE");
    apiHelper.makeMove(sessionId, player0Id, moveRequest);

    // THEN the last-move endpoint returns that move
    ResponseEntity<Map> response = apiHelper.getLastMove(sessionId);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody(), "200 response should contain the last MoveResponse");
    assertNotNull(
        response.getBody().get("result"), "MoveResponse should carry a result field");

    apiHelper.stopGame(sessionId);
  }
}