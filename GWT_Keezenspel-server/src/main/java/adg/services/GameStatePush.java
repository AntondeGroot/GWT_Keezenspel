package adg.services;

import com.adg.openapi.model.Card;
import com.adg.openapi.model.MoveResponse;
import com.adg.openapi.model.Pawn;
import com.adg.openapi.model.Player;
import java.util.List;
import java.util.Map;

/**
 * Personalized SSE payload: standard game state fields plus the receiving player's
 * private card hand and public card counts for all players.
 */
public class GameStatePush {

  private String currentPlayerId;
  private List<Pawn> pawns;
  private List<Player> players;
  private List<String> winners;
  private long version;
  private MoveResponse lastMoveResponse;

  // Public card data (same for everyone)
  private List<String> playedCards;
  private Map<String, Integer> nrOfCardsPerPlayer;

  // Private card data (specific to the receiving player)
  private List<Card> playerCards;

  public String getCurrentPlayerId() { return currentPlayerId; }
  public void setCurrentPlayerId(String v) { currentPlayerId = v; }

  public List<Pawn> getPawns() { return pawns; }
  public void setPawns(List<Pawn> v) { pawns = v; }

  public List<Player> getPlayers() { return players; }
  public void setPlayers(List<Player> v) { players = v; }

  public List<String> getWinners() { return winners; }
  public void setWinners(List<String> v) { winners = v; }

  public long getVersion() { return version; }
  public void setVersion(long v) { version = v; }

  public MoveResponse getLastMoveResponse() { return lastMoveResponse; }
  public void setLastMoveResponse(MoveResponse v) { lastMoveResponse = v; }

  public List<String> getPlayedCards() { return playedCards; }
  public void setPlayedCards(List<String> v) { playedCards = v; }

  public Map<String, Integer> getNrOfCardsPerPlayer() { return nrOfCardsPerPlayer; }
  public void setNrOfCardsPerPlayer(Map<String, Integer> v) { nrOfCardsPerPlayer = v; }

  public List<Card> getPlayerCards() { return playerCards; }
  public void setPlayerCards(List<Card> v) { playerCards = v; }
}