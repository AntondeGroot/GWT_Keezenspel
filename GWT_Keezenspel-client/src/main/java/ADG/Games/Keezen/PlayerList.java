package ADG.Games.Keezen;

import ADG.Games.Keezen.Player.Player;
import ADG.Games.Keezen.dto.PlayerDTO;
import com.google.gwt.core.client.JsArray;
import java.util.ArrayList;

public class PlayerList {

  private JsArray<PlayerDTO> players;
  private boolean isUpToDate;

  public void refresh() {
    isUpToDate = false;
  }

  public void setPlayers(JsArray<PlayerDTO> players) {
    if (this.players.equals(players)) {
      isUpToDate = true;
    } else {
      isUpToDate = false;
      this.players = players;
    }
  }

  public boolean isIsUpToDate() {
    return isUpToDate;
  }
}
