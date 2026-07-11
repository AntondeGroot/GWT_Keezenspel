package adg.keezen;

import com.adg.openapi.model.Card;
import com.adg.openapi.model.Player;
import java.util.*;

public class CardsDeck implements CardsDeckInterface {

  private static final int CARD_VALUES_PER_SUIT = 13; // Ace (1) .. King (13)
  private static final int SUITS = 4;
  private static final int FIRST_CARD_UUID = 100; // non-zero sentinel: a filled uuid is never 0/null
  private static final int CARDS_DEALT_FIRST_ROUND = 5;
  private static final int CARDS_DEALT_LATER_ROUNDS = 4;
  private static final int ROUNDS_BEFORE_RESHUFFLE = 3;

  private int roundNr;
  private ArrayDeque<Card> cardsDeque = new ArrayDeque<>();
  private final ArrayList<Card> playedCards = new ArrayList<>();
  private final HashMap<String, PlayerHand> playerHands = new HashMap<>();
  private ArrayList<String> activePlayers = new ArrayList<>();
  private GameState gameState;

  public CardsDeck() {}

  public void setGameState(GameState gameState) {
    this.gameState = gameState;
  }

  public void addPlayers(ArrayList<Player> players) {
    for (Player p : players) {
      playerHands.put(p.getId(), new PlayerHand());
    }
  }

  public HashMap<String, Integer> getNrOfCardsPerPlayer() {
    HashMap<String, Integer> nrOfCards = new HashMap<>();
    for (Map.Entry<String, PlayerHand> p : playerHands.entrySet()) {
      nrOfCards.put(p.getKey(), p.getValue().getHand().size());
    }
    return nrOfCards;
  }

  /** This player's cards — the live hand list (callers guard the unknown-id case). */
  private ArrayList<Card> handOf(String playerId) {
    return playerHands.get(playerId).getHand();
  }

  public ArrayList<Card> getCardsForPlayer(String playerUUID) {
    if (playerHands.containsKey(playerUUID)) {
      return handOf(playerUUID);
    } else {
      return new ArrayList<>();
    }
  }

  public void forfeitCardsForPlayer(String playerId) {
    playedCards.addAll(handOf(playerId));
    playerHands.get(playerId).dropCards();
  }

  public void shuffleIfFirstRound() {
    if (roundNr != 0) {
      return;
    }

    ArrayList<Card> cards = new ArrayList<>();
    activePlayers = gameState.getActivePlayers();
    // One full suit of values per player (suits cycle 0..3 when there are more than four players).
    int uniqueCardNr = FIRST_CARD_UUID;
    for (int suit = 0; suit < activePlayers.size(); suit++) {
      for (int cardValue = 1; cardValue <= CARD_VALUES_PER_SUIT; cardValue++) {
        cards.add(new Card().suit(suit % SUITS).value(cardValue).uuid(uniqueCardNr++));
      }
    }

    // shuffle the cards
    Collections.shuffle(cards);
    cardsDeque = new ArrayDeque<>(cards);
  }

  public void playCard(String playerId, Card card) {
    if (card == null) {
      return;
    }
    handOf(playerId).remove(card);
    playedCards.add(card);
  }

  public boolean playerHasCardsLeft(String playerId) {
    return !handOf(playerId).isEmpty();
  }

  public void giveCardToPlayerForTesting(String playerId, Card card) {
    // this way you can replace one card by another, play a card in a Test, and then know based on
    // the game whether the player should have 5 or 4 cards in their hand left.
    if (!handOf(playerId).isEmpty()) {
      handOf(playerId).removeFirst();
    }
    handOf(playerId).addFirst(card);
  }

  public void setPlayerCard(String playerId, Card card) {
    handOf(playerId).add(card);
  }

  public void dealCards() {
    int nrCards;
    if (roundNr == 0) {
      // new round so reset played cards stack
      playedCards.clear();
      nrCards = CARDS_DEALT_FIRST_ROUND;
    } else {
      nrCards = CARDS_DEALT_LATER_ROUNDS;
    }

    // Hands should already be empty here (a fresh round is only dealt once every player is
    // inactive, and every path to inactive — forfeit, leave, win — drops that player's cards), but
    // clear them anyway so a deal always yields exactly nrCards regardless of any stray card.
    for (PlayerHand hand : playerHands.values()) {
      hand.dropCards();
    }

    for (int j = 0; j < nrCards; j++) {
      for (Player player : gameState.getPlayers()) {
        if (shouldReceiveCards(player)) {
          setPlayerCard(player.getId(), cardsDeque.pop());
        }
      }
    }

    roundNr = (roundNr + 1) % ROUNDS_BEFORE_RESHUFFLE;
  }

  /** A player is dealt in while active and not yet placed (place &lt; 0 means no medal yet). */
  private static boolean shouldReceiveCards(Player player) {
    Boolean active = player.getIsActive();
    Integer place = player.getPlace();
    return active != null && active && place != null && place < 0;
  }

  public boolean playerHasCard(String playerId, Card card) {
    return handOf(playerId).contains(card);
  }

  public void moveCardBetweenHands(String fromPlayerId, String toPlayerId, Card card) {
    handOf(fromPlayerId).remove(card);
    handOf(toPlayerId).add(card);
  }

  public boolean playerDoesNotHaveCard(String playerId, Card card) {
    return !playerHasCard(playerId, card);
  }

  public void reset() {
    roundNr = 0;
    cardsDeque.clear();
    playerHands.clear();
    playedCards.clear();
  }

  public ArrayList<Card> getPlayedCards() {
    return new ArrayList<>(playedCards);
  }
}
