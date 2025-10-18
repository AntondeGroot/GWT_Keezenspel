package ADG.util;

import com.adg.openapi.model.Player;

public class PlayerStatus {
  public static boolean hasFinished(Player player) {
    if(player.getPlace() == null){
      return false;
    }
    return player.getPlace() > 0;
  }

  public static void setActive(Player player){
    player.setIsActive(true);
  }

  public static void setInactive(Player player){
    player.setIsActive(false);
  }
}
