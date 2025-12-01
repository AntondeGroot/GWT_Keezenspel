package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JsArray;
import java.util.ArrayList;

/**
 * DTO representing a GameState object returned by the Keezen Game API. Compatible with GWT overlay
 * types for JSON parsing via JsonUtils.safeEval().
 */
public class GameStateClient {

  private ArrayList<PawnClient> pawns = new ArrayList<>();
  private String playerIdTurn;
  private ArrayList<PlayerClient> players = new ArrayList<>();

  public GameStateClient(GameStateDTO gameStateDTO) {
    playerIdTurn = gameStateDTO.getCurrentPlayerId();

    // convert players
    ArrayList<PlayerClient> tempPlayers = new ArrayList<>();
    JsArray<PlayerDTO> playersDTO = gameStateDTO.getPlayers();
    for (int i = 0; i < playersDTO.length(); i++) {
      tempPlayers.add(new PlayerClient(playersDTO.get(i)));
    }
    players = tempPlayers;

    // convert pawns
    JsArray<PawnDTO> pawnsDTO = gameStateDTO.getPawns();
    ArrayList<PawnClient> tempPawns = new ArrayList<>();
    for (int i = 0; i < pawnsDTO.length(); i++) {
      tempPawns.add(new PawnClient(pawnsDTO.get(i)));
    }
    pawns = tempPawns;
  }

  public String getCurrentPlayerId() {
    return this.playerIdTurn;
  }

  public ArrayList<PawnClient> getPawns() {
    return this.pawns;
  }

  public ArrayList<PlayerClient> getPlayers() {
    return this.players;
  }
}
