package adg.keezen.IntegrationTests;

import static org.junit.jupiter.api.Assertions.*;

import adg.keezen.ApiUtils.ApiUtil;
import adg.keezen.utils.ApiCallsHelper;
import adg.keezen.utils.BaseIntegrationTest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

  @Test
  void leaveGame_pushesSSEUpdate_toRemainingPlayers() throws Exception {
    // GIVEN player 1 is subscribed to the SSE stream
    String sseUrl = "http://localhost:4200/sse/gamestates/" + sessionId + "/" + player1Id;
    // Each SSE event ends with a blank line; the first event is the initial subscription push.
    // The second event should arrive after player 0 leaves.
    CompletableFuture<Boolean> secondEventReceived = new CompletableFuture<>();

    Thread sseReader = new Thread(() -> {
      try {
        HttpURLConnection conn = (HttpURLConnection) new URL(sseUrl).openConnection();
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.connect();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
          int blankLineCount = 0;
          String line;
          while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
              blankLineCount++;
              if (blankLineCount >= 2) {
                secondEventReceived.complete(true);
                return;
              }
            }
          }
        }
      } catch (Exception e) {
        secondEventReceived.completeExceptionally(e);
      }
    });
    sseReader.setDaemon(true);
    sseReader.start();

    // Allow time for the SSE connection to be established before triggering the leave
    Thread.sleep(300);

    // WHEN player 0 leaves
    ApiUtil.leaveGame(sessionId, player0Id);

    // THEN the remaining player's SSE stream receives the update within a reasonable timeout
    assertTrue(secondEventReceived.get(3, TimeUnit.SECONDS),
        "SSE push must be sent to remaining players when a player leaves");
  }
}