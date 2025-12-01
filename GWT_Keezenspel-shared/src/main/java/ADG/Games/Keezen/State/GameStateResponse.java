package ADG.Games.Keezen.State;

import ADG.Games.Keezen.Player.Pawn;
import ADG.Games.Keezen.Player.Player;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameStateResponse implements IsSerializable {
  private ArrayList<Pawn> pawns;
  private ArrayList<Player> players;
  private HashMap<String, Integer> playerColors;
  private String playerIdTurn;
  private int nrPlayers;
  private ArrayList<String> activePlayers;
  private ArrayList<String> winners;
  private int animationSpeed;

  public GameStateResponse() {}

  public void setPlayerColors(HashMap<String, Integer> playerColors) {
    this.playerColors = playerColors;
  }

  public HashMap<String, Integer> getPlayerColors() {
    return playerColors;
  }

  public ArrayList<Pawn> getPawns() {
    return pawns;
  }

  public void setPawns(ArrayList<Pawn> pawns) {
    this.pawns = pawns;
  }

  // todo: you could remove activePlayers and winners as that info is already contained within
  // Players

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public void setPlayers(ArrayList<Player> players) {
    this.players = players;
  }

  public String getPlayerIdTurn() {
    return playerIdTurn;
  }

  public void setPlayerIdTurn(String playerIdTurn) {
    this.playerIdTurn = playerIdTurn;
  }

  public int getNrPlayers() {
    return nrPlayers;
  }

  public void setNrPlayers(int nrPlayers) {
    this.nrPlayers = nrPlayers;
  }

  public ArrayList<String> getActivePlayers() {
    return activePlayers;
  }

  public void setActivePlayers(ArrayList<String> activePlayers) {
    this.activePlayers = activePlayers;
  }

  public ArrayList<String> getWinners() {
    return winners;
  }

  public void setWinners(ArrayList<String> winners) {
    this.winners = winners;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GameStateResponse that = (GameStateResponse) o;
    return nrPlayers == that.nrPlayers
        && pawnsEqualByIdAndPosition(this.pawns, that.pawns)
        && Objects.equals(players, that.players)
        && Objects.equals(playerColors, that.playerColors)
        && Objects.equals(playerIdTurn, that.playerIdTurn)
        && Objects.equals(activePlayers, that.activePlayers)
        && Objects.equals(winners, that.winners);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        pawns, players, playerColors, playerIdTurn, nrPlayers, activePlayers, winners);
  }

  /***
   * Normal pawn comparison only compares if they have the sameId
   * This also compares current position which is needed to update the PawnAndCardSelection
   * Otherwise it will think a pawn on the board is still in the nest
   * @param list1
   * @param list2
   * @return
   */
  private static boolean pawnsEqualByIdAndPosition(List<Pawn> list1, List<Pawn> list2) {
    if (list1 == null || list2 == null) return false;
    if (list1.size() != list2.size()) return false;

    for (int i = 0; i < list1.size(); i++) {
      Pawn p1 = list1.get(i);
      Pawn p2 = list2.get(i);
      if (!p1.equalsByIdAndPosition(p2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    ArrayList<Pawn> activePawns =
        pawns.stream()
            .filter(c -> c.getCurrentTileId().getTileNr() >= 0)
            .collect(Collectors.toCollection(ArrayList::new));

    return "GameStateResponse{\n"
        + "    active pawns = "
        + activePawns
        + ",\n"
        + "    playerIdTurn = "
        + playerIdTurn
        + ",\n"
        + "    nrPlayers = "
        + nrPlayers
        + ",\n"
        + "    activePlayers = "
        + activePlayers
        + ",\n"
        + "    winners="
        + winners
        + ",\n"
        + '}';
  }

  public int getAnimationSpeed() {
    return animationSpeed;
  }

  public void setAnimationSpeed(int animationSpeed) {
    this.animationSpeed = animationSpeed;
  }
}
