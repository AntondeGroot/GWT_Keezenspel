package ADG.Games.Keezen.ApiUtils;

import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomRoomName;

import ADG.Games.Keezen.utils.ApiCallsHelper;
import com.adg.openapi.model.Player;

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
}
