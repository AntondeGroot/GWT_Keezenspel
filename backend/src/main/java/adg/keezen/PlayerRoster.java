package adg.keezen;

import com.adg.openapi.model.Player;
import java.util.List;
import java.util.Map;

/**
 * The player roster and seating order: look players up by id, find a teammate, and walk the turn
 * order forwards/backwards. Extracted from GameState as pure queries over the (live) players list
 * and the seat map (playerId → seat int); GameState keeps thin delegating methods.
 */
class PlayerRoster {

  private final List<Player> players;
  private final Map<String, Integer> playerColors;

  PlayerRoster(List<Player> players, Map<String, Integer> playerColors) {
    this.players = players;
    this.playerColors = playerColors;
  }

  Player findById(String playerId) {
    for (Player player : players) {
      if (player.getId().equals(playerId)) {
        return player;
      }
    }
    return null;
  }

  String teammateOf(String playerId) {
    Player player = findById(playerId);
    Integer teamId = player == null ? null : player.getTeamId();
    if (teamId == null) {
      return null;
    }
    for (Player other : players) {
      if (!other.getId().equals(playerId) && teamId.equals(other.getTeamId())) {
        return other.getId();
      }
    }
    return null;
  }

  boolean sameTeam(String playerA, String playerB) {
    Player a = findById(playerA);
    Player b = findById(playerB);
    return a != null && b != null && a.getTeamId() != null && a.getTeamId().equals(b.getTeamId());
  }

  List<Player> teamMembers(int teamId) {
    return players.stream().filter(p -> Integer.valueOf(teamId).equals(p.getTeamId())).toList();
  }

  String nextPlayerId(String playerId) {
    int playerInt = playerColors.get(playerId);
    return seatOwner((playerInt + 1) % players.size());
  }

  String previousPlayerId(String playerId) {
    int playerInt = playerColors.get(playerId);
    return seatOwner((playerInt + players.size() - 1) % players.size());
  }

  /** The player sitting in the given seat, or "0" if the seat is unassigned. */
  private String seatOwner(int seat) {
    return playerColors.entrySet().stream()
        .filter(entry -> entry.getValue().equals(seat))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse("0");
  }
}
