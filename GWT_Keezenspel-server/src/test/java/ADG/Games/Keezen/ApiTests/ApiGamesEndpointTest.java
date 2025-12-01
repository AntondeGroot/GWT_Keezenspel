package ADG.Games.Keezen.ApiTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomPlayer;
import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomRoomName;
import static com.adg.openapi.model.GameInfo.StatusEnum.IN_PROGRESS;
import static com.adg.openapi.model.GameInfo.StatusEnum.WAITING;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.utils.ApiCallsHelper;
import com.adg.openapi.model.Player;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.springframework.web.client.HttpClientErrorException;

class ApiGamesEndpointTest {

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();
  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
  }

  @Test
  void createGame_isAddedToGamesEndpoint() {
    // GIVEN
    String roomName = getRandomRoomName();

    // WHEN
    String sessionId = apiHelper.createNewGame(roomName, 3);
    var allGames = apiHelper.getAllGames();

    // THEN
    boolean found =
        allGames.stream()
            .anyMatch(
                game ->
                    roomName.equals(game.get("roomName"))
                        && sessionId.equals(game.get("id"))
                        && WAITING.getValue().equals(game.get("status")));

    assertTrue(found, "Newly created game should appear in /games list with status 'waiting'");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void createGameWithMax3Players_add4Players_throws409ErrorCode() {
    // GIVEN a game has started
    String roomName = getRandomRoomName();
    Player player1 = getRandomPlayer();
    Player player2 = getRandomPlayer();
    Player player3 = getRandomPlayer();
    Player player4 = getRandomPlayer();
    String sessionId = apiHelper.createNewGame(roomName, 3);

    // WHEN you add 3 player
    apiHelper.addPlayerToGame(sessionId, player1);
    apiHelper.addPlayerToGame(sessionId, player2);
    apiHelper.addPlayerToGame(sessionId, player3);

    // THEN expect 409 Conflict on the 4th player
    HttpClientErrorException ex =
        assertThrows(
            HttpClientErrorException.Conflict.class,
            () -> {
              apiHelper.addPlayerToGame(sessionId, player4);
            });

    assertEquals(
        409, ex.getStatusCode().value(), "Expected HTTP 409 Conflict when adding 4th player");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void createGameWithMax3Players_startGame_adding3rdPlayerThrows409ErrorCode() {
    // GIVEN a game has started
    String roomName = getRandomRoomName();
    Player player1 = getRandomPlayer();
    Player player2 = getRandomPlayer();
    Player player3 = getRandomPlayer();
    String sessionId = apiHelper.createNewGame(roomName, 3);

    // WHEN you add 2 player
    apiHelper.addPlayerToGame(sessionId, player1);
    apiHelper.addPlayerToGame(sessionId, player2);
    apiHelper.startGame(sessionId);

    // THEN expect 409 Conflict when you try to add a new player
    HttpClientErrorException ex =
        assertThrows(
            HttpClientErrorException.Conflict.class,
            () -> {
              apiHelper.addPlayerToGame(sessionId, player3);
            });

    assertEquals(
        409,
        ex.getStatusCode().value(),
        "Expected HTTP 409 Conflict when adding player to a started game");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void createGame_addPlayers_StartGame_StatusIsInProgress() {
    // GIVEN a game has started
    String roomName = getRandomRoomName();
    Player player1 = getRandomPlayer();
    Player player2 = getRandomPlayer();
    Player player3 = getRandomPlayer();
    String id = apiHelper.createNewGame(roomName, 3);

    // WHEN you add 3 players
    apiHelper.addPlayerToGame(id, player1);
    apiHelper.addPlayerToGame(id, player2);
    apiHelper.addPlayerToGame(id, player3);

    // WHEN you start the game
    apiHelper.startGame(id);

    // THEN
    var allGames = apiHelper.getAllGames();
    boolean found =
        allGames.stream()
            .anyMatch(
                game ->
                    roomName.equals(game.get("roomName"))
                        && id.equals(game.get("id"))
                        && IN_PROGRESS.getValue().equals(game.get("status")));

    assertTrue(found, "Newly created game should appear in /games list with status 'IN_PROGRESS'");

    // cleanup
    apiHelper.stopGame(id);
  }
}
