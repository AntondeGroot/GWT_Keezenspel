package adg.keezen.IntegrationTests;

import static org.junit.jupiter.api.Assertions.*;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.ApiCallsHelper;
import adg.keezen.utils.BaseIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Integration tests for the DELETE /games/{sessionId}/players/{playerId} endpoint.
 *
 * Tests verify that:
 * - The endpoint returns 204 No Content
 * - The player's cards are forfeited
 * - The player is marked inactive with no medal
 * - The game is removed from the registry when all players leave
 */
public class LeaveGame_IT extends BaseIntegrationTest {

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();
  private String sessionId;
  private String player0Id;
  private String player1Id;
  private String player2Id;

  @BeforeEach
  void setUp() {
    sessionId = ApiUtil.createStandardGame();
    player0Id = ApiUtil.getPlayerid(sessionId, 0);
    player1Id = ApiUtil.getPlayerid(sessionId, 1);
    player2Id = ApiUtil.getPlayerid(sessionId, 2);
  }

  @AfterEach
  void tearDown() {
    // Only stop if the game still exists (might have been removed by a test)
    try {
      apiHelper.stopGame(sessionId);
    } catch (Exception ignored) {
    }
  }

  @Test
  void leaveGame_returns204() {
    int status = ApiUtil.leaveGame(sessionId, player0Id);
    assertEquals(204, status, "leaveGame should return 204 No Content");
  }

  @Test
  void leaveGame_withUnknownSession_returns404() {
    assertThrows(HttpClientErrorException.NotFound.class,
        () -> ApiUtil.leaveGame("non-existent-session", player0Id),
        "leaveGame with an unknown sessionId should return 404");
  }

  @Test
  void afterLeaveGame_playerIsInactive_withNoMedal() {
    // WHEN player 0 leaves
    ApiUtil.leaveGame(sessionId, player0Id);

    // THEN the player is inactive and has no medal
    List<Map<String, Object>> players = apiHelper.getAllPlayersInGame(sessionId);
    Map<String, Object> player0 = players.stream()
        .filter(p -> player0Id.equals(p.get("id")))
        .findFirst()
        .orElseThrow();

    assertNotEquals(Boolean.TRUE, player0.get("isActive"),
        "Leaver should not be active");
    assertNotEquals(Boolean.TRUE, player0.get("isPlaying"),
        "Leaver should not be playing");
    assertEquals(-1, player0.get("place"),
        "Leaver should have no medal (place = -1)");
  }

  @Test
  void afterLeaveGame_playerCardsAreEmpty() {
    // GIVEN player 0 has cards
    List<Map<String, Object>> cardsBefore = apiHelper.getPlayerCards(sessionId, player0Id);
    assertFalse(cardsBefore.isEmpty(), "Player should have cards before leaving");

    // WHEN player 0 leaves
    ApiUtil.leaveGame(sessionId, player0Id);

    // THEN their hand is empty
    List<Map<String, Object>> cardsAfter = apiHelper.getPlayerCards(sessionId, player0Id);
    assertTrue(cardsAfter.isEmpty(), "Leaver's cards should be forfeited");
  }

  @Test
  void whenAllPlayersLeave_gameIsRemovedFromRegistry() {
    // WHEN all three players leave
    ApiUtil.leaveGame(sessionId, player0Id);
    ApiUtil.leaveGame(sessionId, player1Id);
    ApiUtil.leaveGame(sessionId, player2Id);

    // THEN requesting the game returns 404
    assertThrows(HttpClientErrorException.NotFound.class,
        () -> apiHelper.getGameDetails(sessionId),
        "Game should be removed from the registry when all players leave");
  }

  @Test
  void whenOnePlayerLeaves_gameStillExists() {
    // WHEN only one player leaves
    ApiUtil.leaveGame(sessionId, player0Id);

    // THEN the game is still accessible
    Map<String, Object> gameDetails = apiHelper.getGameDetails(sessionId);
    assertNotNull(gameDetails, "Game should still exist when not all players have left");
  }
}