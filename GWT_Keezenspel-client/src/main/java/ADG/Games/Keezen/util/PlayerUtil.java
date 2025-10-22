package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.PlayerDTO;
import java.util.List;

public class PlayerUtil {

  public static PlayerDTO getPlayerById(String playerId, List<PlayerDTO> players) {
    for (PlayerDTO player : players) {
      if (player.getId().equals(playerId)) {
        return player;
      }
    }
    return null;
  }

  public static PlayerDTO getPlayerByInt(int playerInt, List<PlayerDTO> players) {
    for (PlayerDTO player : players) {
      if (player.getPlayerInt() == playerInt) {
        return player;
      }
    }
    return null;
  }
}
