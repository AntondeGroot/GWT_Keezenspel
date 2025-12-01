package ADG.Games.Keezen;

import com.adg.openapi.model.Card;
import com.adg.openapi.model.Player;
import java.util.ArrayList;
import java.util.HashMap;

public interface CardsDeckInterface {

  void addPlayers(ArrayList<Player> players);

  HashMap<String, Integer> getNrOfCardsPerPlayer();

  ArrayList<Card> getCardsForPlayer(String playerUUID);

  void forfeitCardsForPlayer(String playerId);

  void shuffleIfFirstRound();

  boolean playerPlaysCard(String playerId, Card card);

  void giveCardToPlayerForTesting(String playerId, Card card);

  void setPlayerCard(String playerId, Card card);

  void dealCards();

  boolean playerHasCard(String playerId, Card card);

  void reset();

  ArrayList<Card> getPlayedCards();

  void setGameState(GameState gameState);
}
