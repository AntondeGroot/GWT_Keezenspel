package ADG.Games.Keezen.ApiTests;

import static ADG.Games.Keezen.IntegrationTests.Utils.TestUtils.getDriver;
import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomPlayer;
import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomRoomName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.utils.ApiCallsHelper;
import com.adg.openapi.model.Player;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

class ApiGameWithPlayersTest {

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();
  static WebDriver driver;

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
    driver = getDriver();
  }

  @Test
  void createGame_addPlayer_gameContainsThatPlayer() {
    // GIVEN a game has started
    String roomName = getRandomRoomName();
    Player player = getRandomPlayer();
    String sessionId = apiHelper.createNewGame(roomName, 3);

    // WHEN you add a player
    String playerId = apiHelper.addPlayerToGame(sessionId, player);
    player.setId(playerId);

    // THEN they were added to the game
    var players = apiHelper.getAllPlayersInGame(sessionId);

    boolean foundPlayer =
        players.stream()
            .anyMatch(
                p -> p.get("name").equals(player.getName()) && p.get("id").equals(player.getId()));

    assertTrue(foundPlayer, "Newly created game should have newly added player'");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void createGame_add3Players_gameContains3Players() {
    // GIVEN a game has started
    String roomName = getRandomRoomName();
    Player player1 = getRandomPlayer();
    Player player2 = getRandomPlayer();
    Player player3 = getRandomPlayer();
    String sessionId = apiHelper.createNewGame(roomName, 3);

    // WHEN you add 3 player
    apiHelper.addPlayerToGame(sessionId, player1);
    apiHelper.addPlayerToGame(sessionId, player2);
    apiHelper.addPlayerToGame(sessionId, player3);

    // THEN they were added to the game
    var players = apiHelper.getAllPlayersInGame(sessionId);

    assertEquals(3, players.size(), "Newly created game should have 3 players");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void createGame_add3Players_player1isPlayingAndActiver_OtherPlayersActive() {
    // GIVEN a game has started
    String roomName = getRandomRoomName();
    Player player1 = getRandomPlayer();
    Player player2 = getRandomPlayer();
    Player player3 = getRandomPlayer();
    String sessionId = apiHelper.createNewGame(roomName, 3);

    // WHEN you add 3 player
    // you can't use player.uuid because that would have to be assigned belowe
    apiHelper.addPlayerToGame(sessionId, player1);
    apiHelper.addPlayerToGame(sessionId, player2);
    apiHelper.addPlayerToGame(sessionId, player3);
    apiHelper.startGame(sessionId);

    // THEN they were added to the game
    var players = apiHelper.getAllPlayersInGame(sessionId);

    boolean foundPlayer =
        players.stream()
            .anyMatch(
                p ->
                    p.get("name").equals(player1.getName())
                        && p.get("isActive").equals(true)
                        && p.get("isPlaying").equals(true));
    assertTrue(foundPlayer, "First player should be active and playing");
    boolean foundPlayer2 =
        players.stream()
            .anyMatch(
                p ->
                    p.get("name").equals(player2.getName())
                        && p.get("isActive").equals(true)
                        && p.get("isPlaying").equals(false));
    assertTrue(foundPlayer2, "Second player should only be active but not playing");
    boolean foundPlayer3 =
        players.stream()
            .anyMatch(
                p ->
                    p.get("name").equals(player3.getName())
                        && p.get("isActive").equals(true)
                        && p.get("isPlaying").equals(false));
    assertTrue(foundPlayer3, "Third player should be active but not playing");

    // cleanup
    apiHelper.stopGame(sessionId);
  }
}
