package ADG.Games.Keezen.dto;

import com.google.gwt.core.client.JsArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO representing a GameState object returned by the Keezen Game API. Compatible with GWT overlay
 * types for JSON parsing via JsonUtils.safeEval().
 */
public class GameStateClient {

  private ArrayList<PawnClient> pawns = new ArrayList<>();
  private String playerIdTurn;
  private ArrayList<PlayerClient> players = new ArrayList<>();
  private MoveResponseDTO lastMoveResponse;

  public GameStateClient(GameStateDTO gameStateDTO) {
    playerIdTurn = gameStateDTO.getCurrentPlayerId();
    lastMoveResponse = gameStateDTO.getLastMoveResponse();

    // convert players
    ArrayList<PlayerClient> tempPlayers = new ArrayList<>();
    JsArray<PlayerDTO> playersDTO = gameStateDTO.getPlayers();
    Map<String, Integer> playerIntByPlayerId = new HashMap<>();
    for (int i = 0; i < playersDTO.length(); i++) {
      PlayerDTO playerDTO = playersDTO.get(i);
      tempPlayers.add(new PlayerClient(playerDTO));
      playerIntByPlayerId.put(playerDTO.getId(), playerDTO.getPlayerInt());
    }
    players = tempPlayers;

    // convert pawns — derive color from player index
    JsArray<PawnDTO> pawnsDTO = gameStateDTO.getPawns();
    ArrayList<PawnClient> tempPawns = new ArrayList<>();
    for (int i = 0; i < pawnsDTO.length(); i++) {
      PawnDTO pawnDTO = pawnsDTO.get(i);
      int playerInt = playerIntByPlayerId.getOrDefault(pawnDTO.getPlayerId(), 0);
      tempPawns.add(new PawnClient(pawnDTO, playerInt));
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

  public MoveResponseDTO getLastMoveResponse() {
    return lastMoveResponse;
  }
}
