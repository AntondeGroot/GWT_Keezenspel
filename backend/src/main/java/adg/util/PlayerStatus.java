package adg.util;

import com.adg.openapi.model.Player;

public class PlayerStatus {
  public static boolean hasFinished(Player player) {
    Integer place = player.getPlace();
    if (place == null) {
      return false;
    }
    return place > 0;
  }

  public static void setActive(Player player) {
    player.setIsActive(true);
  }

  public static void setInactive(Player player) {
    player.setIsActive(false);
  }
}
