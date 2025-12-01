package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.PlayerClient;
import java.util.List;

public class PlayerUtil {

  public static PlayerClient getPlayerById(String playerId, List<PlayerClient> players) {
    for (PlayerClient player : players) {
      if (player.getId().equals(playerId)) {
        return player;
      }
    }
    return null;
  }

  public static PlayerClient getPlayerByInt(int playerInt, List<PlayerClient> players) {
    for (PlayerClient player : players) {
      if (player.getPlayerInt() == playerInt) {
        return player;
      }
    }
    return null;
  }
}
