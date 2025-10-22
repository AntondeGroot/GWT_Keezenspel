package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.PawnDTO;
import ADG.Games.Keezen.dto.PlayerDTO;
import com.google.gwt.core.client.JsArray;
import java.util.ArrayList;

public class JsUtilities {

  public static ArrayList<PlayerDTO> playersToArrayList(JsArray<PlayerDTO> players) {
    ArrayList<PlayerDTO> tempPlayers = new ArrayList<>();
    for (int i = 0; i < players.length(); i++) {
      tempPlayers.add(players.get(i));
    }
    return tempPlayers;
  }

  public static ArrayList<PawnDTO> pawnsToArrayList(JsArray<PawnDTO> pawns) {
    ArrayList<PawnDTO> tempPawns = new ArrayList<>();
    for (int i = 0; i < pawns.length(); i++) {
      tempPawns.add(pawns.get(i));
    }
    return tempPawns;
  }
}
