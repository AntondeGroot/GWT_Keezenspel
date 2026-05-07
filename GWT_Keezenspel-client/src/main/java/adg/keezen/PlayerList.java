package adg.keezen;

import adg.keezen.dto.PlayerClient;
import java.util.List;

public class PlayerList {

  private List<PlayerClient> players;
  private boolean isUpToDate;

  public void refresh() {
    isUpToDate = false;
  }

  public void setPlayers(List<PlayerClient> players) {
    if (this.players.equals(players)) {
      isUpToDate = true;
    } else {
      isUpToDate = false;
      this.players = new java.util.ArrayList<>(players);
    }
  }

  public boolean isIsUpToDate() {
    return isUpToDate;
  }
}
