package ADG.Games.Keezen.ApiUtils;

import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomRoomName;

import ADG.Games.Keezen.utils.ApiCallsHelper;
import com.adg.openapi.model.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiUtil {

  private static final ApiCallsHelper apiHelper = new ApiCallsHelper();

  public static String createStandardGame() {
    // GIVEN a game has started
    String sessionId = apiHelper.createNewGame(getRandomRoomName(), 3);
    for (int i = 0; i < 3; i++) {
      Player player = new Player("Player" + i, "player " + i);
      apiHelper.addPlayerToGame(sessionId, player);
    }
    apiHelper.startGame(sessionId);

    return sessionId;
  }

  public static List<String> getPlayerIds(String sessionId) {
    List<Map<String, Object>> result = apiHelper.getAllPlayersInGame(sessionId);
    return result.stream().map(map -> (String) map.get("id")).toList();
  }

  public static String getPlayerid(String sessionId, int indexOfPlayer) {
    List<Map<String, Object>> result = apiHelper.getAllPlayersInGame(sessionId);
    return result.get(indexOfPlayer).get("id").toString();
  }
}
