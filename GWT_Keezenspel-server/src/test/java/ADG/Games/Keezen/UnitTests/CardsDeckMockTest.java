package ADG.Games.Keezen.UnitTests;

import static ADG.Games.Keezen.UnitTests.GameStateUtil.createGame_With_NPlayers;
import static ADG.Games.Keezen.UnitTests.GameStateUtil.givePlayerCard;
import static ADG.Games.Keezen.UnitTests.GameStateUtil.place4PawnsOnFinish;
import static ADG.Games.Keezen.UnitTests.GameStateUtil.playRemainingCards;
import static ADG.Games.Keezen.UnitTests.GameStateUtil.sendValidMoveRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.CardsDeckInterface;
import ADG.Games.Keezen.CardsDeckMock;
import ADG.Games.Keezen.GameSession;
import ADG.Games.Keezen.GameState;
import com.adg.openapi.model.Card;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardsDeckMockTest {

  private GameState gameState;
  private CardsDeckInterface cardsDeck;

  @BeforeEach
  void setup() {
    GameSession engine = new GameSession(new CardsDeckMock());
    gameState = engine.getGameState();
    cardsDeck = engine.getCardsDeck();
    cardsDeck.reset();
    gameState.tearDown();
  }

  @AfterEach
  void tearDown() {
    cardsDeck.reset();
    gameState.tearDown();
  }

  @Test
  void fivePlayers_ShuffleDeck_EachHas13Cards() {
    // GIVEN WHEN
    createGame_With_NPlayers(gameState, 5);

    int totalCards = 0;
    for (int i = 0; i < 5; i++) {
      totalCards += cardsDeck.getCardsForPlayer(String.valueOf(i)).size();
    }
    // THEN, all the players have 13 cards in their hand
    assertEquals(5 * 13, totalCards);
  }

  @Test
  void onePlayer_ShuffleDeck_TheyHave5Cards() {
    // GIVEN WHEN
    createGame_With_NPlayers(gameState, 1);

    // THEN
    List<Card> cards = cardsDeck.getCardsForPlayer("0");
    assertTrue(isSortedNumerically(cards));
    assertEquals(13, cards.size());
  }

  @Test
  void dealCards_13_13_13_CardsPerRound() {
    // GIVEN WHEN
    createGame_With_NPlayers(gameState, 1);

    // THEN
    Assert.assertEquals(13, cardsDeck.getCardsForPlayer("0").size());
    cardsDeck.forfeitCardsForPlayer("0");
    cardsDeck.dealCards();

    Assert.assertEquals(13, cardsDeck.getCardsForPlayer("0").size());
    cardsDeck.forfeitCardsForPlayer("0");
    cardsDeck.dealCards();

    Assert.assertEquals(13, cardsDeck.getCardsForPlayer("0").size());
    cardsDeck.forfeitCardsForPlayer("0");
    cardsDeck.shuffleIfFirstRound();
    cardsDeck.dealCards();

    Assert.assertEquals(13, cardsDeck.getCardsForPlayer("0").size());
  }

  @Test
  void twoPlayersHaveTheSameCard() {
    // GIVEN WHEN
    createGame_With_NPlayers(gameState, 2);

    // WHEN
    List<Card> cards1 = cardsDeck.getCardsForPlayer("0");
    List<Card> cards2 = cardsDeck.getCardsForPlayer("1");

    // THEN
    assertFalse(doNotContainTheSameCards(cards1, cards2));
  }

  @Test
  void playerHasCard() {
    // GIVEN WHEN
    createGame_With_NPlayers(gameState, 1);

    // WHEN
    List<Card> cards = cardsDeck.getCardsForPlayer("0");

    // THEN
    assertEquals(13, cards.size());
    for (Card card : cards) {
      assertTrue(cardsDeck.playerHasCard("0", card));
    }
  }

  @Test
  void playerForfeitsCards() {
    // GIVEN
    createGame_With_NPlayers(gameState, 2);

    // WHEN
    cardsDeck.forfeitCardsForPlayer("0");

    // THEN
    HashMap<String, Integer> nrs = new HashMap<>();
    nrs.put("0", 0);
    nrs.put("1", 13);

    assertEquals(nrs, cardsDeck.getNrOfCardsPerPlayer());
    assertTrue(cardsDeck.getCardsForPlayer("0").isEmpty());
  }

  public static boolean isSortedNumerically(List<Card> cards) {
    for (int i = 0; i < cards.size() - 1; i++) {
      int currentValue = cards.get(i).getValue();
      int nextValue = cards.get(i + 1).getValue();
      if (currentValue > nextValue) {
        return false;
      }
    }
    return true;
  }

  public static boolean doNotContainTheSameCards(List<Card> cards1, List<Card> cards2) {
    for (Card card : cards1) {
      if (cards2.contains(card)) {
        return false;
      }
    }
    return true;
  }

  @Test
  void getCardsForPlayer() {
    // GIVEN
    createGame_With_NPlayers(gameState, 8);

    // WHEN
    Card card = givePlayerCard(cardsDeck, 1, -4);

    // THEN
    assertTrue(cardsDeck.getCardsForPlayer("1").contains(card));
    assertTrue(cardsDeck.playerHasCard("1", card));
  }

  @Test
  void playerHasCard2() {
    // GIVEN WHEN
    createGame_With_NPlayers(gameState, 3);

    // WHEN
    place4PawnsOnFinish(gameState, "1");
    gameState.checkForWinners(new ArrayList<>());
    place4PawnsOnFinish(gameState, "2");
    gameState.checkForWinners(new ArrayList<>());
    playRemainingCards(gameState, cardsDeck, "0");

    cardsDeck.shuffleIfFirstRound();
    cardsDeck.dealCards();

    // THEN
    int totalCards =
        cardsDeck.getNrOfCardsPerPlayer().values().stream().mapToInt(Integer::intValue).sum();

    assertEquals(13, totalCards);
  }

  @Test
  void PlayedCardsDoNotResetDuringFirstRound() {
    createGame_With_NPlayers(gameState, 3);
    gameState.setPlayerIdTurn("0");
    for (int i = 0; i < 4; i++) {
      sendValidMoveRequest(gameState, cardsDeck, "0");
      sendValidMoveRequest(gameState, cardsDeck, "1");
      sendValidMoveRequest(gameState, cardsDeck, "2");
    }
    assertEquals(3 * 4, cardsDeck.getPlayedCards().size());

    gameState.tearDown();
    cardsDeck.reset();
  }

  @Test
  void PlayedCardsDoNotResetAfterFirstRound() {
    createGame_With_NPlayers(gameState, 3);
    gameState.setPlayerIdTurn("0");
    for (int i = 0; i < 5; i++) {
      sendValidMoveRequest(gameState, cardsDeck, "0");
      sendValidMoveRequest(gameState, cardsDeck, "1");
      sendValidMoveRequest(gameState, cardsDeck, "2");
    }
    assertEquals(3 * 5, cardsDeck.getPlayedCards().size());

    gameState.tearDown();
    cardsDeck.reset();
  }

  @Test
  void PlayedCardsDoNotResetAfterSecondRound() {
    createGame_With_NPlayers(gameState, 3);
    gameState.setPlayerIdTurn("0");
    for (int i = 0; i < 9; i++) {
      sendValidMoveRequest(gameState, cardsDeck, "0");
      sendValidMoveRequest(gameState, cardsDeck, "1");
      sendValidMoveRequest(gameState, cardsDeck, "2");
    }
    assertEquals(3 * 5 + 3 * 4, cardsDeck.getPlayedCards().size());

    gameState.tearDown();
    cardsDeck.reset();
  }
}
