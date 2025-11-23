package ADG.Games.Keezen.util;

import ADG.Games.Keezen.dto.PawnDTO;
import ADG.Games.Keezen.dto.PlayerDTO;
import ADG.Games.Keezen.dto.PositionKeyDTO;
import com.google.gwt.core.client.JsArray;
import java.util.ArrayList;
import java.util.List;

public class JsUtilities {

  public static List<PlayerDTO> playersToArrayList(JsArray<PlayerDTO> players) {
    ArrayList<PlayerDTO> tempPlayers = new ArrayList<>();
    for (int i = 0; i < players.length(); i++) {
      tempPlayers.add(players.get(i));
    }
    return tempPlayers;
  }

  public static List<PawnDTO> pawnsToArrayList(JsArray<PawnDTO> pawns) {
    ArrayList<PawnDTO> tempPawns = new ArrayList<>();
    for (int i = 0; i < pawns.length(); i++) {
      tempPawns.add(pawns.get(i));
    }
    return tempPawns;
  }

  public static List<PositionKeyDTO> sequenceToArrayList(JsArray<PositionKeyDTO> positions) {
    ArrayList<PositionKeyDTO> tempPositions = new ArrayList<>();
    for (int i = 0; i < positions.length(); i++) {
      tempPositions.add(positions.get(i));
    }
    return tempPositions;
  }
}
