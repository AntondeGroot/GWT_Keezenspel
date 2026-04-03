package ADG.Games.Keezen.ApiUtils;

import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomRoomName;

import ADG.Games.Keezen.utils.ApiCallsHelper;
import com.adg.openapi.model.Player;
import java.util.List;
import java.util.Map;

public class ApiUtil {

  private static final ApiCallsHelper apiHelper = new ApiCallsHelper();

  public static String createStandardGame() {
    // GIVEN a game has started
    String sessionId = apiHelper.createNewGame(getRandomRoomName(), 3);
    for (int i = 0; i < 3; i++) {
      // todo: maybe an identical playername and playerId is not smart
      Player player = new Player("player" + i, "player" + i);
      apiHelper.addPlayerToGame(sessionId, player);
    }
    apiHelper.startGame(sessionId);

    return sessionId;
  }

  public static String createTwoPlayerGame() {
    String sessionId = apiHelper.createNewGame(getRandomRoomName(), 2);
    for (int i = 0; i < 2; i++) {
      Player player = new Player("player" + i, "player" + i);
      apiHelper.addPlayerToGame(sessionId, player);
    }
    apiHelper.startGame(sessionId);
    return sessionId;
  }

  public static List<String> getPlayerIds(String sessionId) {
    List<Map<String, Object>> result = apiHelper.getAllPlayersInGame(sessionId);
    return result.stream().map(map -> (String) map.get("id")).toList();
  }

  public static void setCardForPlayer(String playerId, int cardValue) {
    apiHelper.setCardForPlayer(playerId, cardValue);
  }

  public static void setCardForPlayer(String sessionId, String playerId, int cardValue) {
    apiHelper.setCardForPlayer(sessionId, playerId, cardValue);
  }

  public static String getPlayerid(String sessionId, int indexOfPlayer) {
    List<Map<String, Object>> result = apiHelper.getAllPlayersInGame(sessionId);
    return result.get(indexOfPlayer).get("id").toString();
  }

  public static String getPlayerIdTurn(String sessionId) {
    List<Map<String, Object>> players = apiHelper.getAllPlayersInGame(sessionId);
    for (Map<String, Object> player : players) {
      if (Boolean.TRUE.equals(player.get("isPlaying"))) {
        return (String) player.get("id");
      }
    }
    return null;
  }

  public static void forfeitPlayerViaApi(String sessionId, String playerId) {
    apiHelper.playerForfeits(sessionId, playerId);
  }

  public static void setPawnPosition(String sessionId, String playerId, int pawnNr, String sectionOwnerId, int tileNr) {
    apiHelper.setPawnPosition(sessionId, playerId, pawnNr, sectionOwnerId, tileNr);
  }

  public static int leaveGame(String sessionId, String playerId) {
    return apiHelper.leaveGame(sessionId, playerId);
  }
}
