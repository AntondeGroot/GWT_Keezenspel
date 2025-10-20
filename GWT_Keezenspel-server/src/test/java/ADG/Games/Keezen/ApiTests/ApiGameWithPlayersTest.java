package ADG.Games.Keezen.ApiTests;

import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomPlayer;
import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomRoomName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.utils.ApiCallsHelper;
import com.adg.openapi.model.Player;
import org.junit.jupiter.api.Test;

class ApiGameWithPlayersTest {

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

    boolean foundPlayer = players.stream().anyMatch(p ->
        p.get("name").equals(player.getName()) &&
            p.get("id").equals(player.getId()));

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
}
