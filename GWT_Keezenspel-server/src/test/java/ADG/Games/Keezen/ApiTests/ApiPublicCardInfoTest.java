package ADG.Games.Keezen.ApiTests;

import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomPlayer;
import static ADG.Games.Keezen.utils.ApiModelHelpers.getRandomRoomName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ADG.Games.Keezen.IntegrationTests.Utils.SpringAppTestHelper;
import ADG.Games.Keezen.utils.ApiCallsHelper;
import com.adg.openapi.model.Player;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiPublicCardInfoTest {

  private final ApiCallsHelper apiHelper = new ApiCallsHelper();

  @BeforeEach
  public void setUp() {
    Assumptions.assumeTrue(System.getenv("CI") == null, "Skipping Selenium tests in CI");
    SpringAppTestHelper.startRealApp();
  }

  @Test
  void startGame_publicCardInfo_returnsNrOfCardsPerPlayer() {
    // GIVEN a started game with 3 players
    String sessionId = apiHelper.createNewGame(getRandomRoomName(), 3);
    Player player1 = getRandomPlayer();
    Player player2 = getRandomPlayer();
    Player player3 = getRandomPlayer();
    String id1 = apiHelper.addPlayerToGame(sessionId, player1);
    String id2 = apiHelper.addPlayerToGame(sessionId, player2);
    String id3 = apiHelper.addPlayerToGame(sessionId, player3);
    apiHelper.startGame(sessionId);

    // WHEN fetching public card info
    Map<String, Object> cardInfo = apiHelper.getPubliclyAvailableCardInformation(sessionId);

    // THEN each player has cards
    Map<String, Integer> nrOfCardsPerPlayer =
        (Map<String, Integer>) cardInfo.get("nrOfCardsPerPlayer");
    assertNotNull(nrOfCardsPerPlayer, "nrOfCardsPerPlayer should not be null");
    assertEquals(3, nrOfCardsPerPlayer.size(), "Should have an entry for each player");
    assertTrue(nrOfCardsPerPlayer.get(id1) > 0, "Player 1 should have cards");
    assertTrue(nrOfCardsPerPlayer.get(id2) > 0, "Player 2 should have cards");
    assertTrue(nrOfCardsPerPlayer.get(id3) > 0, "Player 3 should have cards");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void startGame_playedCardsIsEmpty_beforeAnyAction() {
    // GIVEN a started game
    String sessionId = apiHelper.createNewGame(getRandomRoomName(), 3);
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.startGame(sessionId);

    // WHEN fetching public card info before any move
    Map<String, Object> cardInfo = apiHelper.getPubliclyAvailableCardInformation(sessionId);

    // THEN played cards pile is empty
    List<String> playedCards = (List<String>) cardInfo.get("playedCards");
    assertNotNull(playedCards, "playedCards should not be null");
    assertTrue(playedCards.isEmpty(), "No cards should be played yet");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void playerForfeits_playedCardsContainsForfeitedCards() {
    // GIVEN a started game with 3 players
    String sessionId = apiHelper.createNewGame(getRandomRoomName(), 3);
    Player player1 = getRandomPlayer();
    String id1 = apiHelper.addPlayerToGame(sessionId, player1);
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.startGame(sessionId);

    int cardsBefore =
        ((Map<String, Integer>)
                apiHelper.getPubliclyAvailableCardInformation(sessionId).get("nrOfCardsPerPlayer"))
            .get(id1);

    // WHEN the first player (whose turn it is) forfeits
    apiHelper.playerForfeits(sessionId, id1);

    // THEN the forfeited cards appear in the played cards pile
    Map<String, Object> cardInfo = apiHelper.getPubliclyAvailableCardInformation(sessionId);
    List<String> playedCards = (List<String>) cardInfo.get("playedCards");
    assertNotNull(playedCards, "playedCards should not be null");
    assertFalse(playedCards.isEmpty(), "Forfeited cards should appear in the played cards pile");
    assertEquals(cardsBefore, playedCards.size(), "All forfeited cards should be in played pile");

    // cleanup
    apiHelper.stopGame(sessionId);
  }

  @Test
  void playedCards_haveCorrectFormat() {
    // GIVEN a started game where the first player forfeits
    String sessionId = apiHelper.createNewGame(getRandomRoomName(), 3);
    String id1 = apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.addPlayerToGame(sessionId, getRandomPlayer());
    apiHelper.startGame(sessionId);
    apiHelper.playerForfeits(sessionId, id1);

    // WHEN fetching played cards
    List<String> playedCards =
        (List<String>)
            apiHelper.getPubliclyAvailableCardInformation(sessionId).get("playedCards");

    // THEN each entry has the "suit_value" format parseable as two integers
    for (String cardId : playedCards) {
      String[] parts = cardId.split("_");
      assertEquals(2, parts.length, "Card ID '" + cardId + "' should be in 'suit_value' format");
      int suit = Integer.parseInt(parts[0]);
      int value = Integer.parseInt(parts[1]);
      assertTrue(suit >= 0 && suit <= 3, "Suit should be 0-3, got: " + suit);
      assertTrue(value >= 1 && value <= 13, "Value should be 1-13, got: " + value);
    }

    // cleanup
    apiHelper.stopGame(sessionId);
  }
}