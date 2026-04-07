package adg.keezen.ApiTests;

import static adg.keezen.utils.ApiModelHelpers.getRandomPlayer;
import static adg.keezen.utils.ApiModelHelpers.getRandomRoomName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import adg.keezen.utils.ApiCallsHelper;
import adg.keezen.utils.BaseUnitTest;
import com.adg.openapi.model.Player;
import org.junit.jupiter.api.Test;

class ApiGameWithPlayersTest extends BaseUnitTest {

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();

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
  void createGame_add3Players_exactlyOnePlayerIsPlayingAndAllAreActive() {
    // GIVEN a game has started
    String roomName = getRandomRoomName();
    String sessionId = apiHelper.createNewGame(roomName, 3);

    // WHEN you add 3 players and start the game
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.startGameForTesting(sessionId);

    // THEN exactly one player is playing and all are active
    var players = apiHelper.getAllPlayersInGame(sessionId);

    long playingCount = players.stream().filter(p -> p.get("isPlaying").equals(true)).count();
    long activeCount = players.stream().filter(p -> p.get("isActive").equals(true)).count();

    assertEquals(1, playingCount, "Exactly one player should be playing");
    assertEquals(3, activeCount, "All players should be active");

    // cleanup
    apiHelper.stopGame(sessionId);
  }
}
